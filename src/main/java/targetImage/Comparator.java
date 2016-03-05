package targetImage;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

//	private static List<Mat> srcMats;
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

	public static CompareResult getCompareResult() {
//		createResourceMats();

		Mat targetMat = targetImageData.getCurrentMat();
		Map<Rect, Mat> targetFaces = ImageContoroller.getFaceMapFromsrcMat(targetMat);
		Set<Rect> targetKeySet = targetFaces.keySet();

		Map<Rect, Map<String, Double>> resultMaps = new HashMap<>();

		targetKeySet.stream().parallel().forEach((targetKey) -> {
			Mat targetFace = targetFaces.get(targetKey);
			Map<String, Double> resultMap = new HashMap<>();
			Set<String> srcKeySet = resourceMats.keySet();

			srcKeySet.stream().parallel().forEach((srcKey) -> {
				Double minDistance = getMinOfDistance(resourceMats.get(srcKey), targetFace);
				resultMap.put(srcKey, minDistance);
			});

			Double average = srcKeySet.stream().collect(Collectors.averagingDouble(key -> resultMap.get(key)));

			if (average > 1) {
				resultMap.put("average", average);
				resultMaps.put(targetKey, resultMap);
			}
		});
				
		Mat resultMat = createResultMat(targetMat, resultMaps);

		return new CompareResult(resultMaps, resultMat);
	}

	private static Mat createResultMat(Mat targetMat, Map<Rect, Map<String, Double>> resultMaps) {
		Set<Rect> faceKeys = resultMaps.keySet();
		
		Rect maxKey = faceKeys.stream().collect(Collectors.maxBy((key1, key2) -> {
			Double result = (resultMaps.get(key1).get("average") - resultMaps.get(key2).get("average")) * 1000;
			return result.intValue();
		})).orElse(new Rect());
		
		Rect minKey = faceKeys.stream().collect(Collectors.minBy((key1, key2) -> {
			Double result = (resultMaps.get(key1).get("average") - resultMaps.get(key2).get("average")) * 1000;
			return result.intValue();
		})).orElse(new Rect());
		
		int faceNum = 1;
		for (Rect faceKey : faceKeys) {
			String scoreText =  BigDecimal.valueOf(resultMaps.get(faceKey).get("average")).setScale(1, RoundingMode.HALF_UP).toString();
		
			if (faceKey.equals(minKey)) {
				Imgproc.rectangle(targetMat, new Point(faceKey.x, faceKey.y),
						new Point(faceKey.x + faceKey.width, faceKey.y + faceKey.height), new Scalar(0, 255, 0), 6);
				Imgproc.putText(targetMat, String.valueOf(faceNum), new Point(faceKey.x, faceKey.y), 1, 5, new Scalar(255, 255, 0), 6);
				Imgproc.putText(targetMat, scoreText, new Point(faceKey.x + faceKey.width, faceKey.y + faceKey.height), 1, 5, new Scalar(0, 0, 255), 6);
			} else {
				Imgproc.rectangle(targetMat, new Point(faceKey.x, faceKey.y),
						new Point(faceKey.x + faceKey.width, faceKey.y + faceKey.height), new Scalar(0, 200, 0), 3);
				Imgproc.putText(targetMat, String.valueOf(faceNum), new Point(faceKey.x, faceKey.y), 1, 2, new Scalar(200, 200, 0), 4);
				Imgproc.putText(targetMat, scoreText, new Point(faceKey.x + faceKey.width, faceKey.y + faceKey.height), 1, 2, new Scalar(0, 0, 255), 3);
			}
			faceNum++;
		}
		return targetMat;
	}

	public static void createResourceMats(List<File> srcFiles) {
		String directoryPath = ImageContoroller.RESOURCES_PATH + File.separator + "learning";
		String regex = "^.*\\.jpg$";
//		srcMats = ImageContoroller.filesToMats(ImageContoroller.listFile(directoryPath, regex));

		resourceMats = ImageContoroller.filesToMapOfMat(ImageContoroller.listFile(directoryPath, regex));
//		resourceMats = ImageContoroller.filesToMapOfMat(srcFiles);
	}

	public static Double getMinOfDistance(Mat srcMat, Mat targetMat) {
		Mat descriptionOfSrc = calcDescriptor(srcMat);
		Mat descriptionOfTarget = calcDescriptor(targetMat);
		MatOfDMatch matches = matchDescriptors(descriptionOfSrc, descriptionOfTarget);
		return calcMinOfDist(matches);
	}

	static final private List<Integer> detectors = Arrays.asList(FeatureDetector.AKAZE,FeatureDetector.BRISK,FeatureDetector.DENSE,FeatureDetector.GFTT);
	static final private List<Integer> extractors = Arrays.asList(DescriptorExtractor.AKAZE,DescriptorExtractor.BRISK,DescriptorExtractor.ORB);
	static final private List<Integer> matchers = Arrays.asList(DescriptorMatcher.BRUTEFORCE_HAMMING,DescriptorMatcher.BRUTEFORCE);
	
	private static Mat calcDescriptor(Mat srcMat) {
		MatOfKeyPoint keyPoint = new MatOfKeyPoint();// 特徴点
		FeatureDetector featureDetector = FeatureDetector.create(detectors.get(2));
		featureDetector.detect(srcMat, keyPoint);
		Mat descriptor = new Mat();// 特徴量
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(extractors.get(1));
		descriptorExtractor.compute(srcMat, keyPoint, descriptor);
		return descriptor;
	}

	private static MatOfDMatch matchDescriptors(Mat queryDescriptor, Mat trainDescriptor) {
		MatOfDMatch matches = new MatOfDMatch();// 特徴量同士の比較結果
		DescriptorMatcher matcher = DescriptorMatcher.create(matchers.get(0));
		matcher.match(queryDescriptor, trainDescriptor, matches);
		return matches;
	}

	private static Double calcMinOfDist(MatOfDMatch matches) {
		return matches.toList().stream()
				.mapToDouble(item -> Float.valueOf(item.distance).doubleValue())
				.min().orElse(10000D);
	}

}
