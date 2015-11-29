package targetImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javafx.util.Pair;

public class ImageContoroller {

	public final static String RESOURCES_PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator
			+ "main" + File.separator + "resources";

	/**
	 * 画像ファイルから Mat オブジェクトを作成
	 * 
	 * @param file
	 * @return
	 */
	public static Mat fileToMat(File file) {
		// TODO:file のエラーチェック
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

	/**
	 * List<File> の画像ファイルから取り出した Mat のリストを返す
	 * 
	 * @param files
	 * @return List<Mat>
	 */
	public static List<Mat> filesToMats(List<File> files) {
		List<Mat> mats = new ArrayList<>();
		for (File file : files) {
			mats.add(fileToMat(file));
		}
		return mats;
	}

	public static Map<String, Mat> filesToMapOfMat(List<File> files) {
		Map<String, Mat> mats = new HashMap<>();
		for (File file : files) {
			mats.put(file.getName(), fileToMat(file));
		}
		return mats;
	}

	/**
	 * List<File> の画像ファイルから取り出した WritableImage と File の Pair のリストを返す
	 * 
	 * @param files
	 * @return
	 */
	public static List<Pair<File, WritableImage>> filesToImages(List<File> files) {
		List<Pair<File, WritableImage>> images = new ArrayList<>();
		for (File file : files) {
			Pair<File, WritableImage> image = new Pair<>(file, MatToWRImage(fileToMat(file)));
			images.add(image);
		}
		return images;
	}

	/**
	 * 指定されたディレクトリから正規表現に一致するファイルのリストを作成し返す．
	 * 
	 * @param directoryPath
	 * @param regex
	 * @return List<File>
	 */
	public static List<File> listFile(String directoryPath, String regex) {
		File srcDir = new File(directoryPath);
		if (!srcDir.isDirectory())
			throw new IllegalArgumentException(srcDir.getPath() + " is not directory.");
		File[] allFiles = srcDir.listFiles();
		List<File> listFile = new ArrayList<>();
		if (allFiles.length == 0)
			return listFile;
		for (File file : allFiles) {
			boolean isMatch = file.isFile() && file.getName().matches(regex);
			if (isMatch)
				listFile.add(file);
		}
		return listFile;
	}

	/**
	 * srcMat の顔部分を検出し、そこに rectangle を配置した Mat を返す
	 * 
	 * @param srcMat
	 * @return Mat
	 */
	public static Mat addRectangle(Mat srcMat) {
		MatOfRect faceDetections = detectFace(srcMat);
		for (int i = 0; i < faceDetections.toArray().length; i++) {
			Rect rect = faceDetections.toArray()[i];
			String name = "Face: " + (i + 1);
			Imgproc.rectangle(srcMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0), 6);
			Imgproc.putText(srcMat, name, new Point(rect.x, rect.y), 1, 5, new Scalar(0, 0, 255), 6);
		}
		System.out.println(String.format("HelloCv : → Detected %s faces", faceDetections.toArray().length));
		return srcMat;
	}

	public static WritableImage addRectangleToWRImage(Mat srcMat) {
		return MatToWRImage(addRectangle(srcMat));
	}

	private static MatOfRect detectFace(Mat srcMat) {
		CascadeClassifier faceDetector = new CascadeClassifier(
				ImageContoroller.class.getClassLoader().getResource("lbpcascade_frontalface.xml").getPath());
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(srcMat, faceDetections);
		return faceDetections;
	}

	//
	public static void getSourceFaceImage(File imageFile) {
		List<Mat> faceList = getFacesFromFile(imageFile);
		if (faceList.toArray().length != 1) {
			System.out.println("HelloCv : 検出失敗 or 複数検出" + imageFile.getName());
			return;
		}
		String filename = "tmp";
		Imgcodecs.imwrite(filename, faceList.get(0));
	}

	// 画像ファイルから検出した顔のリストを取り出す
	public static List<Mat> getFacesFromFile(File imageFile) {
		Mat srcMat = fileToMat(imageFile);
		return getFaceListFromsrcMat(srcMat);
	}

	// srcMatから検出した顔のリストを取り出す
	public static List<Mat> getFaceListFromsrcMat(Mat srcMat) {
		MatOfRect faceDetections = detectFace(srcMat);
		List<Mat> faceList = new ArrayList<>();
		for (Rect rect : faceDetections.toArray()) {
			faceList.add(new Mat(srcMat, rect));
		}
		return faceList;
	}

	// srcMatから検出した顔のマップを取り出す
	public static Map<Rect, Mat> getFaceMapFromsrcMat(Mat srcMat) {
		MatOfRect faceDetections = detectFace(srcMat);
		Map<Rect, Mat> faceMap = new HashMap<>();
		for (Rect rect : faceDetections.toArray()) {
			faceMap.put(rect, new Mat(srcMat, rect));
		}
		return faceMap;
	}

	public static Mat detectFaceToAddEffect(Mat srcMat) {
		MatOfRect faceDetections = detectFace(srcMat);
		System.out.println(String.format("HelloCv : → Detected %s faces", faceDetections.toArray().length));

		for (Rect rect : faceDetections.toArray()) {
			Mat effectedMat = srcMat.clone();
			Imgproc.cvtColor(effectedMat, effectedMat, Imgproc.COLOR_RGB2GRAY);
			Imgproc.cvtColor(effectedMat, effectedMat, Imgproc.COLOR_GRAY2RGB);

			Mat mask = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type(), new Scalar(0, 0, 0));
			Imgproc.rectangle(mask, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(255, 255, 255), -1);
			srcMat = effectToRoI(srcMat, effectedMat, mask);
		}
		return srcMat;
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
		// mask：srcMat と同じ大きさで色が白（0,0,0）のMat作成
		Mat mask = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type(), new Scalar(0, 0, 0));
		// 効果をかけたい部分だけを黒（255,255,255）にする
		Imgproc.circle(mask, new Point(mask.cols() / 2, mask.rows() / 2), mask.cols() / 3, new Scalar(255, 255, 255),
				-1);
		// effectedMat：srcMat に対し掛けたい効果を全体に掛けたMat
		Mat effectedMat = srcMat.clone();
		Imgproc.cvtColor(effectedMat, effectedMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(effectedMat, effectedMat, Imgproc.COLOR_GRAY2RGB);
		// srcMat に対し effectedMat を mask で黒（255,255,255）の部分のみ合わせ込む
		return effectToRoI(srcMat, effectedMat, mask);
	}

	private static Mat effectToRoI(Mat srcMat, Mat effectedMat, Mat mask) {
		Core.bitwise_and(effectedMat, mask, effectedMat);// effectedMat から mask
															// 以外の部分を消す
		Core.bitwise_not(mask, mask);// mask の領域を反転
		Core.bitwise_and(srcMat, mask, srcMat);// srcMat から mask 以外の領域を消す
		Mat dst = new Mat();
		Core.bitwise_or(srcMat, effectedMat, dst);// 2つを足し合わせる
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
