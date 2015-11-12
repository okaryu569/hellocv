package targetImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

public class Comparator {

	private static List<Mat> srcMats;

	/**
	 * targetMat から顔を検出し、リスト tgtFaces を作成。
	 * 
	 * @param srcMats
	 *            比較元の Mat リスト
	 * @param targetMat
	 *            比較対象の Mat
	 * @return
	 */
	public static List<List<Float>> getResultList(Mat targetMat) {
		createResourceMats();

		List<List<Float>> resultsList = new ArrayList<>();
		List<Mat> tgtFaces = ImageContoroller.getFacesFromsrcMat(targetMat);
		for (Mat tgtFace : tgtFaces) {
			List<Float> result = new ArrayList<>();
			for (Mat srcMat : srcMats) {
				result.add(getMinOfDistance(srcMat, tgtFace));
			}
			resultsList.add(result);
		}

		// Pair<List<List<Float>>,Mat> result = new Pair<List<List<Float>>,
		// Mat>(resultsList, value);

		return resultsList;
	}

	private static void createResourceMats() {
		String directoryPath = ImageContoroller.RESOURCES_PATH + File.separator + "learning";
		String regex = "^.*\\.jpg$";
		srcMats = ImageContoroller.filesToMats(ImageContoroller.listFile(directoryPath, regex));
	}

	public static Float getMinOfDistance(Mat srcMat, Mat targetMat) {
		Mat descriptionOfSrc = calcDescriptor(srcMat);
		Mat descriptionOfTarget = calcDescriptor(targetMat);
		MatOfDMatch matches = matchDescriptors(descriptionOfSrc, descriptionOfTarget);
		Float minOfDistance = calcMinOfDist(matches);
		return minOfDistance;
	}

	private static Mat calcDescriptor(Mat srcMat) {
		MatOfKeyPoint keyPoint = new MatOfKeyPoint();// 特徴点
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.BRISK);
		featureDetector.detect(srcMat, keyPoint);
		Mat descriptor = new Mat();// 特徴量
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
		descriptorExtractor.compute(srcMat, keyPoint, descriptor);
		return descriptor;
	}

	private static MatOfDMatch matchDescriptors(Mat queryDescriptor, Mat trainDescriptor) {
		MatOfDMatch matches = new MatOfDMatch();// 特徴量同士の比較結果
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		matcher.match(queryDescriptor, trainDescriptor, matches);
		return matches;
	}

	private static Float calcMinOfDist(MatOfDMatch matches) {
		float minOfDistance = 100000;
		for (DMatch item : matches.toList()) {
			minOfDistance = Math.min(minOfDistance, item.distance);
		}
		return minOfDistance;
	}

}
