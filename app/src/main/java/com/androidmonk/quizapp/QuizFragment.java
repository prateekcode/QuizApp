package com.androidmonk.quizapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuizFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private String quizId;
    private TextView quizTitle;
    public static final String TAG = "QUIZ_FRAGMENT_TAG";




    //Firebase Data
    private List<QuestionModel> allQuestionsList = new ArrayList<>();
    private long totalQuestionToAnswer = 10;
    private List<QuestionModel> questionsToAnswer= new ArrayList<>();

    public QuizFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Firestore
        firebaseFirestore = FirebaseFirestore.getInstance();
        quizTitle = view.findViewById(R.id.quiz_title);


        //Get Quiz Id
        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizid();
        totalQuestionToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();

        //Get All questions
        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Questions")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    allQuestionsList = task.getResult().toObjects(QuestionModel.class);
                    //task.getResult().toObjects(QuestionModel.class);
                    //Log.d(TAG, "Question List: " + allQuestionsList.get(1).getQuestion());

                    //pickQuestions
                    pickQuestions();

                }else {
                    //Error getting question
                    quizTitle.setText("Error Loading Data");
                }
            }
        });
    }

    private void pickQuestions() {
        for (int i=0; i< totalQuestionToAnswer; i++){
            int randomNumber = getRandomInteger(allQuestionsList.size() - 1, 0);
            questionsToAnswer.add(allQuestionsList.get(randomNumber));
            allQuestionsList.remove(randomNumber);
            Log.d(TAG, "Question: " + i + ":" +  questionsToAnswer.get(i));
        }
    }

    public static int getRandomInteger(int maximum, int minimum){
        return ((int) (Math.random() + (maximum - minimum))) + minimum;
    }
}
