package main;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import targetImage.Comparator;
import targetImage.ImageContoroller;
import targetImage.TargetImageData;

public class MainPaneController implements Initializable {
	@FXML
	private VBox topPane;

	@FXML
	private ScrollPane mainImagePane;

	@FXML
	private ScrollPane srcImagePane;

	@FXML
	private MenuItem saveMenuItem;

	@FXML
	private MenuItem saveAsMenuItem;

	@FXML
	private MenuItem faceDetectMenuItem;

	@FXML
	private MenuItem edgeDetectMenuItem;

	@FXML
	private MenuItem effectMenuItem;

	@FXML
	private MenuItem fitScreenMenuItem;

	@FXML
	private MenuItem originalSizeMenuItem;

	@FXML
	private ImageView mainImageView;

	private TargetImageData targetImageData;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setDisableToMenuItems(true);
		// this.createResourceMats();
		System.out.println("HelloCv : 起動完了");
	}

	private void setDisableToMenuItems(boolean bool) {
		MenuItem menuItems[] = { saveMenuItem, saveAsMenuItem, faceDetectMenuItem, edgeDetectMenuItem, effectMenuItem,
				fitScreenMenuItem, originalSizeMenuItem };
		for (MenuItem item : menuItems) {
			item.setDisable(bool);
		}
	}

	@FXML
	void onOpenMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Open]");
		// TODO:現在のファイルに変更があれば、確認
		File file = this.selectOpenFile();
		if (file == null)
			return;

		targetImageData = new TargetImageData(file);
		mainImageView.setImage(targetImageData.getWRImage());
		this.setDisableToMenuItems(false);
		System.out.println("HelloCv : → Open " + file.getName());
	}

	private File selectOpenFile() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Please select an image file.");
		File file = chooser.showOpenDialog(topPane.getScene().getWindow());
		if (file == null)
			System.out.println("HelloCv : → [Cancel]");
		else
			System.out.println("HelloCv : → [Select]:" + file.getName());
		return file;
	}

	@FXML
	void onSaveMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Save]");
		// TODO:上書き保存
		// TODO:変更がない場合は、押下不可
		targetImageData.saveImage();

	}

	@FXML
	void onSaveAsMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Save As...]");
		File file = selectSaveFile();
		// TODO:別名で保存
		// TODO:変更がない場合は、押下不可
	}

	private File selectSaveFile() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Please select ");
		File file = chooser.showSaveDialog(topPane.getScene().getWindow());
		if (file == null)
			System.out.println("HelloCv : → [Cancel]");
		else
			System.out.println("HelloCv : → [Select]:" + file.getName());
		return file;
	}

	@FXML
	void onQuitMenuItemFired(ActionEvent event) {
		// TODO:変更が保存されていなければ終了確認ダイアログ
		System.out.println("HelloCv : [Quit]");
		Platform.exit();
	}

	@FXML
	void onFaceDetectMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [FaceDetect]");
		Mat faceDetect = ImageContoroller.detectFaceToAddRect(targetImageData.getCurrentMat());
		targetImageData.getImageDataLog().add(faceDetect);
		mainImageView.setImage(targetImageData.getWRImage());
	}

	@FXML
	void onEdgeDetectMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [EdgeDetect]");
		Mat resultOfEffect = ImageContoroller.edgeDetect(targetImageData.getCurrentMat());
		targetImageData.getImageDataLog().add(resultOfEffect);
		mainImageView.setImage(targetImageData.getWRImage());
	}

	/**
	 * Effect テンプレ
	 */
	@FXML
	void onEffectMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Excute Comparison]");

		List<List<Float>> resultList = Comparator.getResultList(targetImageData.getCurrentMat());
		for (int i = 0; i < resultList.size(); i++) {
			System.out.println("HelloCv :  →face " + (i + 1));
			for (Float dist : resultList.get(i)) {
				System.out.println("HelloCv :   Distance = " + dist);
			}
		}

		Mat resultMat = ImageContoroller.detectFaceToAddRect(targetImageData.getCurrentMat());
		mainImageView.setImage(ImageContoroller.MatToWRImage(resultMat));

		System.out.println("HelloCv : [Finish Comparison]");
	}

	private void setSrcImagePane() {
		int numOfSrc = 6;
		VBox srcImgVBox = new VBox(numOfSrc);

		ImageView srcImageView = new ImageView();

		srcImagePane.getChildrenUnmodifiable().add(srcImgVBox);
		// List<ImageView> srcViewList =
		for (Node node : srcImgVBox.getChildren()) {
		}
	}

	@FXML
	void onFitScreenMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Fit Screen Size]");

		mainImageView.setFitHeight(mainImagePane.getHeight());
		mainImageView.setFitWidth(mainImagePane.getWidth());

		mainImagePane.heightProperty().addListener(imageHeightChangeListener);
		mainImagePane.heightProperty().addListener(imageWidthChangeListener);
	}

	@FXML
	void onOriginalSizeMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Original Size] ");
		mainImageView.setFitHeight(targetImageData.getWRImage().getHeight());
		mainImageView.setFitWidth(targetImageData.getWRImage().getWidth());

		mainImagePane.heightProperty().removeListener(imageHeightChangeListener);
		mainImagePane.heightProperty().removeListener(imageWidthChangeListener);
	}

	private ChangeListener<? super Number> imageHeightChangeListener = (ob, o, n) -> {
		mainImageView.setFitHeight(mainImagePane.getHeight());
	};

	private ChangeListener<? super Number> imageWidthChangeListener = (ob, o, n) -> {
		mainImageView.setFitWidth(mainImagePane.getWidth());
	};
}
