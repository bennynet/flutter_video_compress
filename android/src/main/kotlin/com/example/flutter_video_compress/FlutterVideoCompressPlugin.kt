package com.example.flutter_video_compress

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger


class FlutterVideoCompressPlugin : MethodCallHandler,FlutterPlugin {

    private val channelName = "flutter_video_compress"
    private val utility = Utility(channelName)
    private var ffmpegCommander: FFmpegCommander? = null
    lateinit var channel:MethodChannel;
    lateinit var  binding: FlutterPlugin.FlutterPluginBinding;
    companion object {
        //private lateinit var reg: Registrar

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            //val channel = MethodChannel(registrar.messenger(), "flutter_video_compress")
            //channel.setMethodCallHandler(FlutterVideoCompressPlugin())
            //reg = registrar
            var plugin=FlutterVideoCompressPlugin()
            plugin.onAttachedToEngine(registrar.messenger())
        }
    }

    fun onAttachedToEngine(messager: BinaryMessenger){
        this.channel = MethodChannel(messager, "flutter_video_compress")
        this.channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        initFfmpegCommanderIfNeeded()
        when (call.method) {
            "getThumbnail" -> {
                val path = call.argument<String>("path")!!
                val quality = call.argument<Int>("quality")!!
                val position = call.argument<Int>("position")!!.toLong()
                ThumbnailUtility(channelName).getThumbnail(path, quality, position, result)
            }
            "getThumbnailWithFile" -> {
                val path = call.argument<String>("path")!!
                val quality = call.argument<Int>("quality")!!
                val position = call.argument<Int>("position")!!.toLong()
                ThumbnailUtility(channelName).getThumbnailWithFile(binding.applicationContext , path, quality,
                        position, result)
            }
            "getMediaInfo" -> {
                val path = call.argument<String>("path")!!
                result.success(utility.getMediaInfoJson(binding.applicationContext, path).toString())
            }
            "compressVideo" -> {
                val path = call.argument<String>("path")!!
                val quality = call.argument<Int>("quality")!!
                val deleteOrigin = call.argument<Boolean>("deleteOrigin")!!
                val startTime = call.argument<Int>("startTime")
                val duration = call.argument<Int>("duration")
                val includeAudio = call.argument<Boolean>("includeAudio")
                val frameRate = call.argument<Int>("frameRate")

                ffmpegCommander?.compressVideo(path, VideoQuality.from(quality), deleteOrigin,
                        startTime, duration, includeAudio, frameRate, result, binding.binaryMessenger)
            }
            "cancelCompression" -> {
                ffmpegCommander?.cancelCompression()
                result.success("")
            }
            "convertVideoToGif" -> {
                val path = call.argument<String>("path")!!
                val startTime = call.argument<Int>("startTime")!!.toLong()
                val endTime = call.argument<Int>("endTime")!!.toLong()
                val duration = call.argument<Int>("duration")!!.toLong()

                ffmpegCommander?.convertVideoToGif(path, startTime, endTime, duration, result,
                    binding.binaryMessenger)
            }
            "deleteAllCache" -> {
                utility.deleteAllCache(binding.applicationContext, result)
            }
            else -> result.notImplemented()
        }
    }

    private fun initFfmpegCommanderIfNeeded() {
        if (ffmpegCommander == null) {
            ffmpegCommander = FFmpegCommander(binding.applicationContext, channelName)
        }
    }



    override fun onAttachedToEngine(p0: FlutterPlugin.FlutterPluginBinding) {
        binding=p0;
        this.onAttachedToEngine(p0.binaryMessenger)
    }

    override fun onDetachedFromEngine(p0: FlutterPlugin.FlutterPluginBinding) {
        this.channel.setMethodCallHandler(null)
    }
}

