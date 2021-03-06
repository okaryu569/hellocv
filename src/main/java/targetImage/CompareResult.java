package targetImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class CompareResult {
	private Map<Rect, Map<String, Double>> resultMaps;

	private Mat resultMat;

	public CompareResult(Map<Rect, Map<String, Double>> resultMaps, Mat resultMat) {
		super();
		this.resultMaps = resultMaps;
		this.resultMat = resultMat;
	}

	public Map<Rect, Map<String, Double>> getResultMaps() {
		return resultMaps;
	}

	public Mat getResultMat() {
		return resultMat;
	}

	public Double getAverage(Rect key) {
		return this.resultMaps.get(key).get("average");
	}

	public List<Map<String, Double>> getResultList() {
		List<Map<String, Double>> resultList = new ArrayList<>();
		Set<Rect> faceKeys = this.resultMaps.keySet();

		for (Rect faceKey : faceKeys) {
			resultList.add(this.resultMaps.get(faceKey));
		}

		return resultList;
	}

}
