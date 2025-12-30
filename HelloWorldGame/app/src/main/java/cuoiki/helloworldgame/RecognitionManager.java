package cuoiki.helloworldgame;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.common.RecognitionResult;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.recognition.Ink;


public class RecognitionManager {
    private DigitalInkRecognizer recognizer;
    private DigitalInkRecognitionModel model;
    private static final String TAG = "MLKit_v19";

    public interface RecognitionListener {
        void onResult(String result);
    }

    public RecognitionManager() {
        DigitalInkRecognitionModelIdentifier modelIdentifier = null;

        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        } catch (MlKitException e) {
            Log.e(TAG, "Lỗi tạo Model Identifier: " + e.getMessage());
        }

        if (modelIdentifier != null) {
            model = DigitalInkRecognitionModel.builder(modelIdentifier).build();

            // Tạo Client nhận diện
            DigitalInkRecognizerOptions options = DigitalInkRecognizerOptions.builder(model).build();
            recognizer = DigitalInkRecognition.getClient(options);
        }
    }
    public void downloadModel() {
        if (model == null) return;

        RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();

        remoteModelManager.isModelDownloaded(model)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        if (isDownloaded) {
                            Log.d(TAG, "Model đã có sẵn, sẵn sàng sử dụng.");
                        } else {
                            // Chưa có thì tải về
                            Log.d(TAG, "Đang tải model xuống (v19)...");

                            DownloadConditions conditions = new DownloadConditions.Builder()
                                    .build();

                            remoteModelManager.download(model, conditions)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d(TAG, "Tải model THÀNH CÔNG.");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Tải model THẤT BẠI: " + e.getMessage());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Lỗi khi kiểm tra model: " + e.getMessage());
                    }
                });
    }

    // Hàm nhận diện
    public void recognize(Ink ink, final RecognitionListener listener) {
        if (recognizer == null) {
            Log.e(TAG, "Recognizer chưa khởi tạo (Do lỗi model identifier).");
            return;
        }

        recognizer.recognize(ink)
                .addOnSuccessListener(new OnSuccessListener<RecognitionResult>() {
                    @Override
                    public void onSuccess(RecognitionResult result) {
                        if (!result.getCandidates().isEmpty()) {
                            String text = result.getCandidates().get(0).getText();
                            Log.d(TAG, "Kết quả nhận diện: " + text);
                            listener.onResult(text);
                        } else {
                            Log.d(TAG, "Không nhận diện được ký tự nào.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Lỗi trong quá trình nhận diện: " + e.getMessage());
                    }
                });
    }

    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
    }
}