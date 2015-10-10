package main;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import targetImage.ImageContoroller;
import targetImage.TargetImageData;

public class MainPaneController implements Initializable {
	@FXML
	private VBox topPane;

	@FXML
	private ScrollPane mainImagePane;

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
		// chooser.setSelectedExtensionFilter(new
		// ExtensionFilter("ImageFiles"));
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
		// TODO:別名で保存
		// TODO:変更がない場合は、押下不可
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
		Mat faceDetect = ImageContoroller.faceDetect(targetImageData.getCurrentMat());
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
		System.out.println("HelloCv : [Effect]");
		Mat resultOfEffect = ImageContoroller.partEffect(targetImageData.getCurrentMat());
		targetImageData.getImageDataLog().add(resultOfEffect);
		mainImageView.setImage(targetImageData.getWRImage());
	}

	@FXML
	void onFitScreenMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Fit Screen Size]");
		mainImageView.setFitHeight(mainImagePane.getHeight());
		mainImageView.setFitWidth(mainImagePane.getWidth());
	}

	@FXML
	void onOriginalSizeMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Original Size]");
		mainImageView.setFitHeight(targetImageData.getWRImage().getHeight());
		mainImageView.setFitWidth(targetImageData.getWRImage().getWidth());
	}

}
