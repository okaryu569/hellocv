package targetImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.scene.image.WritableImage;

public class ImageData {
	private List<Mat> imageDataLog = new ArrayList<>();
	private File srcFile;
	private int current;

	public ImageData(File srcFile) {
		this.setTgtImage(srcFile);
		this.srcFile = srcFile;
		// this.current = 0;
	}

	private void setTgtImage(File imageFile) {
		Mat srcMat = ImageContoroller.fileToMat(imageFile);
		this.imageDataLog.add(srcMat);
	}

	public List<Mat> getImageDataLog() {
		return imageDataLog;
	}

	// public Mat getImageData(int arrayNo) {
	// if (arrayNo > this.imageDataLog.size())
	// return null;
	// return this.imageDataLog.get(arrayNo - 1);
	// }

	public File getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(File srcFile) {
		this.srcFile = srcFile;
	}

	// TODO:仕様を考えてから実装すること
	// public void updateCurrent() {
	// this.current++;
	// }
	//
	// // TODO: 戻る系統
	// public void undo() {
	// this.current--;
	// }
	//
	// public void redo() {
	// this.current++;
	// }

	public Mat getCurrentMat() {
		return this.imageDataLog.get(imageDataLog.size() - 1);
	}

	public void saveImage() {
		Imgcodecs.imwrite(this.srcFile.getName(), this.getCurrentMat());

		// TODO:apache.commons 入れてファイル移動！ 現状では HelloCV/ に保存
		// String file = System.getProperty("user.dir") + "/tmp/test.txt";
		// String to_file = "/home/test/test.txt";
		// FileUtils.moveFile(file, to_file);
	}

	public WritableImage getWRImage() {
		Mat currentMat = getCurrentMat();
		return ImageContoroller.MatToWRImage(currentMat);
	}

}
