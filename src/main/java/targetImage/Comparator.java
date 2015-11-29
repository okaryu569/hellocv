package targetImage;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

public class Comparator {

	private static final Float INICIAL_VALUE = 100000.0F;

	private static List<Mat> srcMats;
	private static ImageData targetImageData;

	private static Map<String, Mat> resourceMats;

	/**
	 * 比較対象の ImageData を File から設定する．
	 */
	public static void setTargetImageData(File targetFile) {
		targetImageData = new ImageData(targetFile);
	}

	/**
	 * 比較対象の ImageData の最新の Mat を返す
	 * 
	 * @return Mat
	 */
	public static Mat getCurrentMat() {
		return targetImageData.getCurrentMat();
	}

	/**
	 * それぞれの比較元と、比較対象から検出した顔部分との比較結果のスコアを List として返す．
	 * 
	 * @return List<List<Float>>
	 */
	public static CompareResult getCompareResult() {
		createResourceMats();

		Mat targetMat = targetImageData.getCurrentMat();
		Map<Rect, Mat> targetFaces = ImageContoroller.getFaceMapFromsrcMat(targetMat);
		Set<Rect> targetKeySet = targetFaces.keySet();

		Map<Rect, Map<String, Float>> resultMaps = new HashMap<>();

		targetKeySet.stream().parallel().forEach((targetKey) -> {
			Mat targetFace = targetFaces.get(targetKey);
			Map<String, Float> resultMap = new HashMap<>();
			Set<String> srcKeySet = resourceMats.keySet();

			srcKeySet.stream().parallel().forEach((srcKey) -> {
				Float score = getMinOfDistance(resourceMats.get(srcKey), targetFace);
				resultMap.put(srcKey, score);
			});

			Float tmp = 0F;
			for (String srcKey : resultMap.keySet()) {
				tmp += resultMap.get(srcKey);
			}
			Float average = tmp / resourceMats.size();
			if (average < 1000F) {
				resultMap.put("average", average);
				resultMaps.put(targetKey, resultMap);
			}
		});

		Mat resultMat = createResultMat(targetMat, resultMaps);

		return new CompareResult(resultMaps, resultMat);

		// for (Rect targetKey : targetKeySet) {
		// Mat targetFace = targetFaces.get(targetKey);
		// Map<String, Float> resultMap = new HashMap<>();
		// Set<String> srcKeySet = resourceMats.keySet();
		// Float tmp = 0F;
		//
		// for (String srcKey : srcKeySet) {
		// Float score = getMinOfDistance(resourceMats.get(srcKey), targetFace);
		// resultMap.put(srcKey, score);
		// tmp += score;
		// }
		// Float average = tmp / resourceMats.size();
		// resultMap.put("average", average);
		//
		// resultMaps.put(targetKey, resultMap);
		// }
	}

	private static Mat createResultMat(Mat targetMat, Map<Rect, Map<String, Float>> resultMaps) {

		Set<Rect> faceKeys = resultMaps.keySet();
		int faceNum = 1;

		Rect minKey = null;

		for (Rect faceKey : faceKeys) {
			if (minKey == null) {
				minKey = faceKey;
				continue;
			}
			if (resultMaps.get(faceKey).get("average") < resultMaps.get(minKey).get("average"))
				minKey = faceKey;
		}

		for (Rect faceKey : faceKeys) {
			BigDecimal average = new BigDecimal(resultMaps.get(faceKey).get("average").doubleValue());
			String score = faceNum + ": " + average.setScale(1, RoundingMode.HALF_UP);

			if (faceKey.equals(minKey)) {
				Imgproc.rectangle(targetMat, new Point(faceKey.x, faceKey.y),
						new Point(faceKey.x + faceKey.width, faceKey.y + faceKey.height), new Scalar(0, 255, 0), 6);
				Imgproc.putText(targetMat, score, new Point(faceKey.x, faceKey.y), 1, 5, new Scalar(0, 0, 255), 6);
			} else {
				Imgproc.rectangle(targetMat, new Point(faceKey.x, faceKey.y),
						new Point(faceKey.x + faceKey.width, faceKey.y + faceKey.height), new Scalar(0, 200, 0), 3);
				Imgproc.putText(targetMat, score, new Point(faceKey.x, faceKey.y), 1, 2, new Scalar(0, 0, 200), 3);
			}

			faceNum++;
		}
		return targetMat;
	}

	private static void createResourceMats() {
		String directoryPath = ImageContoroller.RESOURCES_PATH + File.separator + "learning";
		String regex = "^.*\\.jpg$";
		srcMats = ImageContoroller.filesToMats(ImageContoroller.listFile(directoryPath, regex));

		resourceMats = ImageContoroller.filesToMapOfMat(ImageContoroller.listFile(directoryPath, regex));
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
		float minOfDistance = INICIAL_VALUE;
		for (DMatch item : matches.toList()) {
			minOfDistance = Math.min(minOfDistance, item.distance);
		}
		return minOfDistance;
	}

}
