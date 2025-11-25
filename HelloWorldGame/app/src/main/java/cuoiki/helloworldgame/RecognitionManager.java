package cuoiki.helloworldgame;

import android.util.Log;

import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import java.util.List;

public class RecognitionManager {
    private DigitalInkRecognizer recognizer;
    private DigitalInkRecognitionModel model;

    public interface RecognitionListener {
        void onResult(String result);
    }

    public RecognitionManager() {
        DigitalInkRecognitionModelIdentifier modelIdentifier = null;
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        } catch (MlKitException e) {
            Log.e("AI", "Lỗi không tìm thấy ngôn ngữ: " + e.getMessage());
            return;
        }

        model = DigitalInkRecognitionModel.builder(modelIdentifier).build();
        recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build());
    }

    // Gọi model AI
    public void downloadModel() {
        if (model == null) return;

        RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();
        remoteModelManager.isModelDownloaded(model)
                .addOnSuccessListener(isDownloaded -> {
                    if (!isDownloaded) {
                        Log.d("AI", "Đang tải model xuống...");
                        remoteModelManager.download(model, new DownloadConditions.Builder().build())
                                .addOnSuccessListener(v -> Log.d("AI", "Model downloaded success"))
                                .addOnFailureListener(e -> Log.e("AI", "Download failed", e));
                    } else {
                        Log.d("AI", "Model đã có sẵn");
                    }
                });
    }

    // Nhận diện nét vẽ
    public void recognize(Ink ink, RecognitionListener listener) {
        if (recognizer == null) {
            Log.e("AI", "Recognizer chưa khởi tạo");
            return;
        }
        recognizer.recognize(ink)
                .addOnSuccessListener(result -> {
                    if (!result.getCandidates().isEmpty()) {
                        String text = result.getCandidates().get(0).getText();
                        Log.d("AI", "Nhận diện được: " + text);
                        listener.onResult(text);
                    } else {
                        Log.d("AI", "Chữ j đây ba???");
                    }
                })
                .addOnFailureListener(e -> Log.e("AI", "Error recognizing", e));
    }
    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
    }
}
