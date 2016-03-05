package main;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Pair;
import targetImage.ImageContoroller;

public class DemoDialogController implements Initializable {


	@FXML
	private Label explanatoryText;

	@FXML
	private ImageView demoImageView;

	private Stage dialogStage;

	private int demoNo = 0;
	
	private List<Pair<File, WritableImage>> demoImages;

	private List<String> explanatories = Arrays.asList(
			"ステップ 1 へ",
			"→ 顔部分の画像の検出",
			"→ 顔部分の画像の切り抜き",
			"ステップ 2 へ",
			"→ 特徴点の抽出",
			"ステップ 3 へ",
			"→ 特徴点の比較の実行",
			"→ 比較結果の数値化",
			"おわり");
	
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("HelloCv : DemoDialog : Start");

		this.setupDemoImages();		
		this.setupExplanatoryText();
		this.setDemoImage();
	}

	private void setupDemoImages() {
		String directoryPath = ImageContoroller.RESOURCES_PATH + File.separator + "demo";
		String regex = "^.*\\.jpg$";
		this.demoImages = ImageContoroller.filesToImages(ImageContoroller.listFile(directoryPath, regex));
	}
	
	
	private void setupExplanatoryText() {
		this.explanatoryText.setAlignment(Pos.CENTER);
		this.explanatoryText.setText(explanatories.get(demoNo));
		explanatoryText.setOnMouseClicked((a) -> {
			if(demoNo == demoImages.size()){
				this.dialogStage.close();
				System.out.println("HelloCv : DemoDialog : Finish");
				return;
			}
			this.explanatoryText.setText(explanatories.get(demoNo));
			this.setDemoImage();
		});

		explanatoryText.setOnMouseEntered((a) -> {
			explanatoryText.setStyle("-fx-font-size: 33px");			
			explanatoryText.setUnderline(true);
		});
		
		explanatoryText.setOnMouseExited((a) -> {
			explanatoryText.setStyle("-fx-font-size: 32px");
			explanatoryText.setUnderline(false);
		});
	}
	
	private void setDemoImage(){
		this.demoImageView.setImage(demoImages.get(demoNo).getValue());
		this.demoImageView.setFitWidth(800);
		this.demoImageView.setFitHeight(450);
//		this.demoImageView.setFitWidth(1080);
//		this.demoImageView.setFitHeight(608);
		demoNo++;
	}
}
