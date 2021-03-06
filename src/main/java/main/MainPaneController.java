package main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import targetImage.Comparator;
import targetImage.CompareResult;
import targetImage.ImageContoroller;

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
	private MenuItem compareMenuItem;

	@FXML
	private MenuItem fitScreenMenuItem;

	@FXML
	private MenuItem originalSizeMenuItem;

	@FXML
	private MenuItem startDemoMenuItem;

	@FXML
	private MenuItem akazeAkazeMenuItem;

	@FXML
	private MenuItem briskBriskMenuItem;
	
	@FXML
	private MenuItem gfttBriskMenuItem;
	
	@FXML
	private ImageView mainImageView;

	@FXML
	private ImageView srcImageView01;

	@FXML
	private Label srcImageLabel01;

	@FXML
	private ImageView srcImageView02;

	@FXML
	private Label srcImageLabel02;

	@FXML
	private ImageView srcImageView03;

	@FXML
	private Label srcImageLabel03;

	@FXML
	private ImageView srcImageView04;

	@FXML
	private Label srcImageLabel04;

	@FXML
	private ImageView srcImageView05;

	@FXML
	private Label srcImageLabel05;

	@FXML
	private TableView<ResultRow> resultTableView;

	@FXML
	private TableColumn<ResultRow, String> faceNumColumn;

	@FXML
	private TableColumn<ResultRow, Float> src01Column;

	@FXML
	private TableColumn<ResultRow, Float> src02Column;

	@FXML
	private TableColumn<ResultRow, Float> src03Column;

	@FXML
	private TableColumn<ResultRow, Float> src04Column;

	@FXML
	private TableColumn<ResultRow, Float> src05Column;

	@FXML
	private TableColumn<ResultRow, Float> src06Column;

	@FXML
	private TableColumn<ResultRow, BigDecimal> averageColumn;

	private Comparator comparator = new Comparator();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setDisableToMenuItems(true);
		// this.setSrcImages();
		this.setResultTableView();

		System.out.println("HelloCv : 起動完了");
	}

	private void setupMenuItem(){
		
	}
	
	
	private void setResultTableView() {
		faceNumColumn.setCellValueFactory(new PropertyValueFactory<>("faceName"));
		src01Column.setCellValueFactory(new PropertyValueFactory<>("score0fSrc01"));
		src02Column.setCellValueFactory(new PropertyValueFactory<>("score0fSrc02"));
		src03Column.setCellValueFactory(new PropertyValueFactory<>("score0fSrc03"));
		src04Column.setCellValueFactory(new PropertyValueFactory<>("score0fSrc04"));
		src05Column.setCellValueFactory(new PropertyValueFactory<>("score0fSrc05"));
		src06Column.setCellValueFactory(new PropertyValueFactory<>("score0fSrc06"));
		averageColumn.setCellValueFactory(new PropertyValueFactory<>("average"));
	}

	private void setDisableToMenuItems(boolean bool) {
		MenuItem menuItems[] = { saveMenuItem, saveAsMenuItem, faceDetectMenuItem, compareMenuItem, fitScreenMenuItem,
				originalSizeMenuItem };
		Arrays.asList(menuItems).stream().forEach(item -> item.setDisable(bool));
	}

	private void setSrcImages() {
		String directoryPath = ImageContoroller.RESOURCES_PATH + File.separator + "learning";
		String regex = "^.*\\.jpg$";
		List<Pair<File, WritableImage>> srcImages = ImageContoroller.filesToImages(ImageContoroller.listFile(
				directoryPath, regex));
		ImageView srcImgViewList[] = { srcImageView01, srcImageView02, srcImageView03, srcImageView04,
				srcImageView05, };
		Label srcImgLabelList[] = { srcImageLabel01, srcImageLabel02, srcImageLabel03, srcImageLabel04,
				srcImageLabel05, };
		for (int i = 0; i < srcImgViewList.length; i++) {
			srcImgViewList[i].setImage(srcImages.get(i).getValue());
			srcImgViewList[i].setFitWidth(120);
			srcImgLabelList[i].setText(srcImages.get(i).getKey().getName());
		}

		this.comparator.setResourceMats();
		
//		FileChooser chooser = new FileChooser();
//		chooser.setTitle("Please select an image file.");
//		List<File> files = chooser.showOpenMultipleDialog(topPane.getScene().getWindow());
//		comparator.createResourceMats(files);
	}

	
	@FXML
	void onSelectTargetMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Select Target Image]");
		File file = this.selectOpenFile();
		if (file == null)
			return;

		this.comparator.setTargetImageData(file);
		mainImageView.setImage(ImageContoroller.MatToWRImage(this.comparator.getCurrentMat()));
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
		// targetImageData.saveImage();
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
		System.out.println("HelloCv : [Quit]");
		Platform.exit();
	}

	@FXML
	void onFaceDetectMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [FaceDetect]");
		WritableImage faceDetect = ImageContoroller.addRectangleToWRImage(this.comparator.getCurrentMat());
		mainImageView.setImage(faceDetect);
	}

	@FXML
	void onSelectSourceMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Select Source Image]");
		this.setSrcImages();
	}

	/**
	 * Effect テンプレ
	 */
	@FXML
	void onCompareMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Excute Comparison]");
		resultTableView.getItems().clear();

		CompareResult compareResult = this.comparator.getCompareResult(FeatureDetector.BRISK, DescriptorExtractor.BRISK);

		List<ResultRow> rows = new ArrayList<>();
		int faceNum = 1;
		for (Map<String, Double> resultMap : compareResult.getResultList()) {
			rows.add(new ResultRow(faceNum, resultMap));
			faceNum++;
		}
		resultTableView.getItems().addAll(rows);

		mainImageView.setImage(ImageContoroller.MatToWRImage(compareResult.getResultMat()));
		System.out.println("HelloCv : [Finish Comparison]");
	}
	
	@FXML
	void onCompareAkazeMenuItemFired(ActionEvent event) {
		this.onCompareFired(FeatureDetector.AKAZE, DescriptorExtractor.AKAZE);
	}

	@FXML
	void onCompareBriskMenuItemFired(ActionEvent event) {
		this.onCompareFired(FeatureDetector.BRISK, DescriptorExtractor.BRISK);
	}
	
	@FXML
	void onCompareGFTTMenuItemFired(ActionEvent event) {
		this.onCompareFired(FeatureDetector.GFTT, DescriptorExtractor.BRISK);
	}
	
	private void onCompareFired(int detector,int extractor){
		System.out.println("HelloCv : [Excute Comparison]");
		resultTableView.getItems().clear();

		CompareResult compareResult = this.comparator.getCompareResult(detector,extractor);

		List<ResultRow> rows = new ArrayList<>();
		int faceNum = 1;
		for (Map<String, Double> resultMap : compareResult.getResultList()) {
			rows.add(new ResultRow(faceNum, resultMap));
			faceNum++;
		}
		resultTableView.getItems().addAll(rows);

		mainImageView.setImage(ImageContoroller.MatToWRImage(compareResult.getResultMat()));
		System.out.println("HelloCv : [Finish Comparison]");
	}
	
	public static class ResultRow {
		private StringProperty faceName = new SimpleStringProperty();
		private DoubleProperty score0fSrc01 = new SimpleDoubleProperty();
		private DoubleProperty score0fSrc02 = new SimpleDoubleProperty();
		private DoubleProperty score0fSrc03 = new SimpleDoubleProperty();
		private DoubleProperty score0fSrc04 = new SimpleDoubleProperty();
		private DoubleProperty score0fSrc05 = new SimpleDoubleProperty();
		private DoubleProperty score0fSrc06 = new SimpleDoubleProperty();
		private DoubleProperty average = new SimpleDoubleProperty();

		public ResultRow(int faceNum, Map<String, Double> resultMap) {
			this.setFaceName(new String("Face " + faceNum));
			this.setScores(resultMap);
		}

		private void setScores(Map<String, Double> resultMap) {
			this.average.set(BigDecimal.valueOf(resultMap.get("average")).setScale(1, RoundingMode.HALF_UP)
					.doubleValue());
			System.out.println("FaceName: " + this.faceName.get() + "  Average: " + this.average.get());

			List<Double> scores = resultMap.keySet().stream().filter(key -> !key.equals("average")).map(key -> {
				Double score = BigDecimal.valueOf(resultMap.get(key)).setScale(1, RoundingMode.HALF_UP).doubleValue();
				System.out.println(" " + key + " : " + score);
				return score;
			}).collect(Collectors.toList());

			this.score0fSrc01.set(scores.get(0));
			this.score0fSrc02.set(scores.get(1));
			this.score0fSrc03.set(scores.get(2));
			this.score0fSrc04.set(scores.get(3));
			this.score0fSrc05.set(scores.get(4));
			this.score0fSrc06.set(scores.get(5));
		}

		public String getFaceName() {
			return faceName.get();
		}

		public void setFaceName(String faceName) {
			this.faceName.set(faceName);
		}

		public Double getScore0fSrc01() {
			return score0fSrc01.get();
		}

		public void setScore0fSrc01(Float score0fSrc01) {
			this.score0fSrc01.set(score0fSrc01);
		}

		public Double getScore0fSrc02() {
			return score0fSrc02.get();
		}

		public void setScore0fSrc02(Float score0fSrc02) {
			this.score0fSrc02.set(score0fSrc02);
		}

		public Double getScore0fSrc03() {
			return score0fSrc03.get();
		}

		public void setScore0fSrc03(Float score0fSrc03) {
			this.score0fSrc03.set(score0fSrc03);
		}

		public Double getScore0fSrc04() {
			return score0fSrc04.get();
		}

		public void setScore0fSrc04(Float score0fSrc04) {
			this.score0fSrc04.set(score0fSrc04);
		}

		public Double getScore0fSrc05() {
			return score0fSrc05.get();
		}

		public void setScore0fSrc05(Float score0fSrc05) {
			this.score0fSrc05.set(score0fSrc05);
		}

		public Double getScore0fSrc06() {
			return score0fSrc06.get();
		}

		public void setScore0fSrc06(Float score0fSrc06) {
			this.score0fSrc06.set(score0fSrc06);
		}

		public Double getAverage() {
			return average.get();
		}

		public void setAverage(Double average) {
			this.average.set(average);
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
		WritableImage wi = ImageContoroller.MatToWRImage(this.comparator.getCurrentMat());
		mainImageView.setFitHeight(wi.getHeight());
		mainImageView.setFitWidth(wi.getWidth());
		mainImagePane.heightProperty().removeListener(imageHeightChangeListener);
		mainImagePane.heightProperty().removeListener(imageWidthChangeListener);
	}

	private ChangeListener<? super Number> imageHeightChangeListener = (ob, o, n) -> {
		mainImageView.setFitHeight(mainImagePane.getHeight());
	};

	private ChangeListener<? super Number> imageWidthChangeListener = (ob, o, n) -> {
		mainImageView.setFitWidth(mainImagePane.getWidth());
	};

	@FXML
	void onStartDemoMenuItemFired(ActionEvent event) {
		System.out.println("HelloCv : [Demo...]");
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(this.getClass().getResource("DemoDialog.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("顔認証 解説 デモ");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);

			DemoDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);

			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
