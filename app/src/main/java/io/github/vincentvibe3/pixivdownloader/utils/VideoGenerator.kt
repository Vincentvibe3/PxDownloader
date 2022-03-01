package io.github.vincentvibe3.pixivdownloader.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.media.*
import android.opengl.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.vincentvibe3.pixivdownloader.R
import io.github.vincentvibe3.pixivdownloader.serialization.UgoiraData
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

//adapted from https://www.sisik.eu/blog/android/media/images-to-video
class VideoGenerator {

    private var presentationTimeUs = 0L
    private var trackIndex: Int = -1
    private val vertexShaderCode =
        "precision highp float;\n" +
                "attribute vec3 vertexPosition;\n" +
                "attribute vec2 uvs;\n" +
                "varying vec2 varUvs;\n" +
                "uniform mat4 mvp;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "\tvarUvs = uvs;\n" +
                "\tgl_Position = mvp * vec4(vertexPosition, 1.0);\n" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;\n" +
                "\n" +
                "varying vec2 varUvs;\n" +
                "uniform sampler2D texSampler;\n" +
                "\n" +
                "void main()\n" +
                "{\t\n" +
                "\tgl_FragColor = texture2D(texSampler, varUvs);\n" +
                "}"

    private var vertices = floatArrayOf(
        // x, y, z, u, v
        -1.0f, -1.0f, 0.0f, 0f, 0f,
        -1.0f, 1.0f, 0.0f, 0f, 1f,
        1.0f, 1.0f, 0.0f, 1f, 1f,
        1.0f, -1.0f, 0.0f, 1f, 0f
    )

    private var indices = intArrayOf(
        2, 1, 0, 0, 3, 2
    )

    private var program: Int = 0
    private var vertexHandle: Int = 0
    private var bufferHandles = IntArray(2)
    private var uvsHandle: Int = 0
    private var mvpHandle: Int = 0
    private var samplerHandle: Int = 0
    private val textureHandle = IntArray(1)

    var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertices)
            position(0)
        }
    }

    var indexBuffer: IntBuffer = ByteBuffer.allocateDirect(indices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asIntBuffer().apply {
            put(indices)
            position(0)
        }
    }

    private fun initGl() {
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also { shader ->
            GLES20.glShaderSource(shader, vertexShaderCode)
            GLES20.glCompileShader(shader)
        }

        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also { shader ->
            GLES20.glShaderSource(shader, fragmentShaderCode)
            GLES20.glCompileShader(shader)
        }

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)

            vertexHandle = GLES20.glGetAttribLocation(it, "vertexPosition")
            uvsHandle = GLES20.glGetAttribLocation(it, "uvs")
            mvpHandle = GLES20.glGetUniformLocation(it, "mvp")
            samplerHandle = GLES20.glGetUniformLocation(it, "texSampler")
        }

        // Initialize buffers
        GLES20.glGenBuffers(2, bufferHandles, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES20.GL_DYNAMIC_DRAW)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indexBuffer, GLES20.GL_DYNAMIC_DRAW)

        // Init texture handle
        GLES20.glGenTextures(1, textureHandle, 0)

        // Ensure I can draw transparent stuff that overlaps properly
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    var width = -1
    var height = -1

    fun generate(files:String, data:UgoiraData, dest:String, id:String, context:Context):Boolean{
        val fileCount = File("${context.cacheDir.absolutePath}/$id").listFiles()?.size ?: return false
        var videoBuildNotif = NotificationCompat.Builder(context, "Downloads")
            .setSmallIcon(R.drawable.download_icon)
            .setContentTitle("Generating $id.webm")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        val dir = File(files)
        val images = dir.listFiles()
        images?.sort()
        images ?: return false
        val bmp = BitmapFactory.decodeFile(images.first().absolutePath)
        val muxer = MediaMuxer("$dest/$id.webm", MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM)
        val roundInitWidth = bmp.width/10*10
        val roundInitHeight = bmp.height/10*10
        val queryFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_VP8, 600, 600)
        queryFormat.setInteger(MediaFormat.KEY_BIT_RATE, 10000000)
        queryFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        queryFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        queryFormat.setLong(MediaFormat.KEY_DURATION, 1)
        queryFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 15)
        val encoderMime = MediaCodecList(MediaCodecList.ALL_CODECS).findEncoderForFormat(queryFormat)
        width = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
            .first { it.name==encoderMime }
            .getCapabilitiesForType("video/x-vnd.on2.vp8")
            .videoCapabilities.supportedWidths.clamp(roundInitWidth)
        height = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
            .first { it.name==encoderMime }
            .getCapabilitiesForType("video/x-vnd.on2.vp8")
            .videoCapabilities.getSupportedHeightsFor(width).clamp(roundInitHeight)

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_VP8, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 10000000)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setLong(MediaFormat.KEY_DURATION, 1)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 15)
        val encoder = MediaCodec.createByCodecName(encoderMime)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = encoder.createInputSurface()

        var eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY)
            throw RuntimeException("eglDisplay == EGL14.EGL_NO_DISPLAY: "
                    + GLUtils.getEGLErrorString(EGL14.eglGetError()))

        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1))
            throw RuntimeException("eglInitialize(): " + GLUtils.getEGLErrorString(EGL14.eglGetError()))

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGLExt.EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val nConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, nConfigs, 0)

        var err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        val ctxAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        var eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)

        err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )
        var eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surface, surfaceAttribs, 0)
        err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext))
            throw RuntimeException("eglMakeCurrent(): " + GLUtils.getEGLErrorString(EGL14.eglGetError()))
        initGl()
        encoder.start()

        var progress = 0
        for (image in images) {
            with(NotificationManagerCompat.from(context)) {
                notify(id.toInt(), videoBuildNotif
                    .setProgress(fileCount, progress, false)
                    .setContentText("Processing ${image.name}")
                    .build())
            }
            val frame = data.body!!.frames.first { it.file == image.name }
            presentationTimeUs += frame.delay*1000L
            val filePath = image.absolutePath
            Log.i("Encoder", "Draining")
            // Get encoded data and feed it to muxer
            drainEncoder(encoder, muxer, false)
            Log.i("Encoder", "Rendering")
            // Render the bitmap/texture with OpenGL here
            val rendered = render(filePath)
            if (!rendered){
                return false
            }
            Log.i("Encoder", "EGL Timestamp")
            // Set timestamp with EGL extension
            EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, presentationTimeUs * 1000)
            Log.i("Encoder", "Swapping Buffers")
            // Feed encoder with next frame produced by OpenGL
            EGL14.eglSwapBuffers(eglDisplay, eglSurface)
            progress++
        }

// Drain last encoded data and finalize the video file

        drainEncoder(encoder, muxer, true)
        encoder.stop()
        encoder.release()

        muxer.stop()
        muxer.release()
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)
        }
        surface.release()
        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE
        return true


    }

    private fun render(imagePath:String):Boolean{
        // Load bitmap from file
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: return false

        // Prepare some transformations
        val mvp = FloatArray(16)
        Matrix.setIdentityM(mvp, 0)
        Matrix.scaleM(mvp, 0, 1f, -1f, 1f)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        GLES20.glViewport(0, 0, bitmap.width, bitmap.height)

        GLES20.glUseProgram(program)

// Pass transformations to shader
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)

// Prepare texture for drawing
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1)

// Pass the Bitmap to OpenGL here
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

// Prepare buffers with vertices and indices & draw
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])

        GLES20.glEnableVertexAttribArray(vertexHandle)
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 4 * 5, 0)

        GLES20.glEnableVertexAttribArray(uvsHandle)
        GLES20.glVertexAttribPointer(uvsHandle, 2, GLES20.GL_FLOAT, false, 4 * 5, 3 * 4)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0)
        return true
    }

    private fun drainEncoder(encoder: MediaCodec, muxer: MediaMuxer, endOfStream: Boolean) {
        if (endOfStream)
            encoder.signalEndOfInputStream()

        while (true) {
            val bufferInfo = MediaCodec.BufferInfo()
            val outBufferId = encoder.dequeueOutputBuffer(bufferInfo, 1000000)
            if (outBufferId >= 0) {
                val encodedBuffer = encoder.getOutputBuffer(outBufferId)

                // MediaMuxer is ignoring KEY_FRAMERATE, so I set it manually here
                // to achieve the desired frame rate
                bufferInfo.presentationTimeUs = presentationTimeUs
                if (encodedBuffer != null) {
                    muxer.writeSampleData(trackIndex, encodedBuffer, bufferInfo)
                }

                encoder.releaseOutputBuffer(outBufferId, false)

                // Are we finished here?
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                    break
            } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream)
                    break

                // End of stream, but still no output available. Try again.
            } else if (outBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                trackIndex = muxer.addTrack(encoder.outputFormat)
                muxer.start()
            }
        }
    }

}