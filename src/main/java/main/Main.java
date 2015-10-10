package main;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void init() throws Exception {
		super.init();
		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				// TODO Auto-generated method stub :ここに Logger とかをセット
				// スレッドがキャッチできない例外をキャッチしてくれる
				System.out.println("キャッチできない例外発生");
				e.printStackTrace();
			}
		});
	}

	@Override
	public void start(Stage primarystage) throws IOException {
		System.out.println("HelloCv : 起動開始...");

		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainPane.fxml"));

		Parent root = loader.load();
		primarystage.setScene(new Scene(root));
		primarystage.setTitle("Hello CV for N1_2015");
		primarystage.show();
	}

	@Override
	public void stop() throws Exception {
		System.out.println("HelloCv : 終了");
		super.stop();
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Application.launch(args);
		// System.out.println("Hello, OpenCV");
		// new DetectFaceDemo().run();
	}

}