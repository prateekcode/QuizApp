package com.androidmonk.quizapp;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FirebaseRepository  {

    private OnFireStoreTaskComplete onFireStoreTaskComplete;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private Query quizRef = firebaseFirestore.collection("QuizList")
            .whereEqualTo("visibility","public" );


    public FirebaseRepository(OnFireStoreTaskComplete onFireStoreTaskComplete){
        this.onFireStoreTaskComplete = onFireStoreTaskComplete;
    }

    public void getQuizData(){
        quizRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    onFireStoreTaskComplete.quizListDataAdded(task.getResult().toObjects(QuizListModel.class));
                }else {
                    onFireStoreTaskComplete.onError(task.getException());
                }
            }
        });
    }

    public interface OnFireStoreTaskComplete{
        void quizListDataAdded(List<QuizListModel> quizListModelList);
        void onError(Exception e);
    }
}
