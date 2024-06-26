package edu.wpi.first.shuffleboard.plugin.cameraserver;

import java.nio.ByteBuffer;

import edu.wpi.first.cscore.ImageSink;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import edu.wpi.first.cscore.CameraServerJNI;
import edu.wpi.first.util.PixelFormat;
import edu.wpi.first.util.RawFrame;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class JavaCvSink extends ImageSink {
  private final RawFrame frame = new RawFrame();
  private Mat tmpMat;
  private ByteBuffer origByteBuffer;
  private int width;
  private int height;
  private PixelFormat pixelFormat;

  private int getCVFormat(PixelFormat pixelFormat) {
    int type = 0;
    switch (pixelFormat) {
      case kYUYV:
      case kRGB565:
        type = CvType.CV_8UC2;
        break;
      case kBGR:
        type = CvType.CV_8UC3;
        break;
      case kGray:
      case kMJPEG:
      default:
        type = CvType.CV_8UC1;
        break;
    }
    return type;
  }

  @Override
  public void close() {
    frame.close();
    super.close();
  }

  /**
   * Create a sink for accepting OpenCV images.
   * WaitForFrame() must be called on the created sink to get each new
   * image.
   *
   * @param name Source name (arbitrary unique identifier)
   */
  public JavaCvSink(String name) {
    super(CameraServerJNI.createRawSink(name, false));
  }

  /**
   * Wait for the next frame and get the image.
   * Times out (returning 0) after 0.225 seconds.
   * The provided image will have three 3-bit channels stored in BGR order.
   *
   * @return Frame time, or 0 on error (call GetError() to obtain the error
   *         message)
   */
  public long grabFrame(Mat image) {
    return grabFrame(image, 0.225);
  }

  /**
   * Wait for the next frame and get the image.
   * Times out (returning 0) after timeout seconds.
   * The provided image will have three 3-bit channels stored in BGR order.
   *
   * @return Frame time, or 0 on error (call GetError() to obtain the error
   *         message); the frame time is in 1 us increments.
   */
  public long grabFrame(Mat image, double timeout) {
    frame.setInfo(0, 0, 0, PixelFormat.kBGR);
    long rv = CameraServerJNI.grabRawSinkFrameTimeout(m_handle, frame, frame.getNativeObj(), timeout);
    if (rv <= 0) {
      return rv;
    }

    if (frame.getData() != origByteBuffer || width != frame.getWidth() || height != frame.getHeight()
        || pixelFormat != frame.getPixelFormat()) {
      origByteBuffer = frame.getData();
      height = frame.getHeight();
      width = frame.getWidth();
      pixelFormat = frame.getPixelFormat();
      if (frame.getStride() == 0) {
        tmpMat = new Mat(frame.getHeight(), frame.getWidth(), getCVFormat(pixelFormat),
                         origByteBuffer);
      } else {
        tmpMat = new Mat(frame.getHeight(), frame.getWidth(), getCVFormat(pixelFormat),
                         origByteBuffer, frame.getStride());
      }
    }
    tmpMat.copyTo(image);
    return rv;
  }

  /**
   * Wait for the next frame and get the image.
   * Times out (returning 0) after timeout seconds.
   * The provided image will have three 3-bit channels stored in BGR order.
   *
   * @return Frame time, or 0 on error (call GetError() to obtain the error
   *         message); the frame time is in 1 us increments.
   */
  public long grabFrameNoTimeout(Mat image) {
    frame.setInfo(0, 0, 0, PixelFormat.kBGR);
    long rv = CameraServerJNI.grabRawSinkFrame(m_handle, frame, frame.getNativeObj());
    if (rv <= 0) {
      return rv;
    }

    if (frame.getData() != origByteBuffer || width != frame.getWidth() || height != frame.getHeight()
        || pixelFormat != frame.getPixelFormat()) {
      origByteBuffer = frame.getData();
      height = frame.getHeight();
      width = frame.getWidth();
      pixelFormat = frame.getPixelFormat();
      if (frame.getStride() == 0) {
        tmpMat = new Mat(frame.getHeight(), frame.getWidth(), getCVFormat(pixelFormat),
                         origByteBuffer);
      } else {
        tmpMat = new Mat(frame.getHeight(), frame.getWidth(), getCVFormat(pixelFormat),
                         origByteBuffer, frame.getStride());
      }
    }
    tmpMat.copyTo(image);
    return rv;
  }
}
