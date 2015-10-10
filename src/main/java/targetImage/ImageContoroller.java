package targetImage;

import java.awt.image.BufferedImage;
import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class ImageContoroller {

	private final static String RESOURCES_PATH = System.getProperty("user.dir") + File.separator + "src"
			+ File.separator + "main" + File.separator + "resources";

	/**
	 * 画像ファイルから Mat オブジェクトを作成
	 * 
	 * @param file
	 * @return
	 */
	public static Mat fromFile(File file) {
		// TODO:
		return Imgcodecs.imread(file.getPath());
	}

	/**
	 * Mat オブジェクトから画像ファイルを作成
	 * 
	 * @param mat
	 * @return
	 */
	public static File fromMat(Mat mat) {
		// TODO:
		return null;
	}

	public static Mat faceDetect(Mat srcMat) {
		CascadeClassifier faceDetector = new CascadeClassifier(
				ImageContoroller.class.getClassLoader().getResource("lbpcascade_frontalface.xml").getPath());
		// CascadeClassifier faceDetector = new CascadeClassifier(
		// RESOURCES_PATH + File.separator + "lbpcascade_frontalface.xml");
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(srcMat, faceDetections);
		for (Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(srcMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0));
		}

		System.out.println(String.format("HelloCv : → Detected %s faces", faceDetections.toArray().length));

		return srcMat;
	}

	public static Mat effect02(Mat srcMat) {
		MatOfRect faceDetections = rectOfFaceDetection(srcMat);
		Mat effectedMat = edgeDetect(srcMat);
		for (Rect rect : faceDetections.toArray()) {
			Mat mask = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type(), new Scalar(0, 0, 0));
			srcMat = roi(srcMat, effectedMat, mask);
		}
		return null;
	}

	private static MatOfRect rectOfFaceDetection(Mat srcMat) {
		CascadeClassifier faceDetector = new CascadeClassifier(
				RESOURCES_PATH + File.separator + "lbpcascade_frontalface.xml");
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(srcMat, faceDetections);
		return faceDetections;
	}

	public static Mat edgeDetect(Mat srcMat) {
		Mat gray = new Mat();
		Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.Canny(gray, gray, 400, 500, 5, true);
		return gray;
	}

	public static Mat effect(Mat srcMat) {
		System.out.println("エフェクト　テンプレ");
		return srcMat;
	}

	public static Mat partEffect(Mat srcMat) {
		Mat mask = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type(), new Scalar(0, 0, 0));
		Imgproc.circle(mask, new Point(mask.cols() / 2, mask.rows() / 2), mask.cols() / 3, new Scalar(255, 255, 255),
				-1);
		Mat effectedMat = srcMat.clone();
		Imgproc.cvtColor(effectedMat, effectedMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(effectedMat, effectedMat, Imgproc.COLOR_GRAY2RGB);
		return roi(srcMat, effectedMat, mask);
	}

	public static Mat roi(Mat srcMat, Mat effectedMat, Mat mask) {
		Core.bitwise_and(effectedMat, mask, effectedMat);// mask 部分の切り抜き
		Core.bitwise_not(mask, mask); // maskを反転します
		Core.bitwise_and(srcMat, mask, srcMat); // 元画像からmask部分を除去して背景画像を準備します
		Mat dst = new Mat();
		Core.bitwise_or(srcMat, effectedMat, dst);
		return dst;
	}

	public static WritableImage MatToWRImage(Mat mat) {
		// TODO: null check
		return BfImageToWrImage(MatToBfImage(mat));
	}

	private static WritableImage BfImageToWrImage(BufferedImage bfImage) {
		WritableImage wrImage = SwingFXUtils.toFXImage(bfImage, null);
		return wrImage;
	}

	private static BufferedImage MatToBfImage(Mat mat) {
		int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
		byte data[] = new byte[dataSize];
		mat.get(0, 0, data);
		int type;
		if (mat.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else {
			type = BufferedImage.TYPE_3BYTE_BGR;
			for (int i = 0; i < dataSize; i += 3) {
				byte blue = data[i + 0];
				data[i + 0] = data[i + 2];
				data[i + 2] = blue;
			}
		}

		BufferedImage bfImage = new BufferedImage(mat.cols(), mat.rows(), type);
		bfImage.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
		return bfImage;
	}

}
