package com.androidmonk.quizapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuizFragment extends Fragment implements View.OnClickListener {

    private FirebaseFirestore firebaseFirestore;
    private String quizId;

    public static final String TAG = "QUIZ_FRAGMENT_TAG";


    //UI Elements
    private TextView quizTitle;
    private Button optionOneBtn;
    private Button optionTwoBtn;
    private Button optionThreeBtn;
    private Button nextBtn;
    private ImageButton closeBtn;
    private TextView questionFeedback;
    private TextView questionText;
    private TextView questionTime;
    private ProgressBar questionProgress;
    private TextView questionNumber;

    private boolean canAnswer = false;
    private int currentQuestion =0;

    private int correctAnswers = 0;
    private int wrongAnswers = 0;


    //Firebase Data
    private List<QuestionModel> allQuestionsList = new ArrayList<>();
    private long totalQuestionToAnswer = 10;
    private List<QuestionModel> questionsToAnswer= new ArrayList<>();
    private CountDownTimer countDownTimer;

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

        //UI Initialize
        quizTitle = view.findViewById(R.id.quiz_title);
        optionOneBtn = view.findViewById(R.id.quiz_option_one);
        optionTwoBtn = view.findViewById(R.id.quiz_option_two);
        optionThreeBtn = view.findViewById(R.id.quiz_option_three);
        nextBtn = view.findViewById(R.id.quiz_next_btn);
        questionFeedback = view.findViewById(R.id.quiz_question_feedback);
        questionText = view.findViewById(R.id.quiz_question);
        questionTime = view.findViewById(R.id.quiz_question_time);
        questionProgress = view.findViewById(R.id.quiz_question_progress);
        questionNumber = view.findViewById(R.id.quiz_question_number);


        //Initialize Firestore
        firebaseFirestore = FirebaseFirestore.getInstance();

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
                    loadUI();

                }else {
                    //Error getting question
                    quizTitle.setText("Error Loading Data");
                }
            }
        });


        //Set Button Click Listener
        optionOneBtn.setOnClickListener(this);
        optionTwoBtn.setOnClickListener(this);
        optionThreeBtn.setOnClickListener(this);

        nextBtn.setOnClickListener(this);
    }

    private void loadUI() {
        // Quiz Data Load, Load the UI Method
        quizTitle.setText("Quiz Data Loaded");

        questionText.setText("Load First Question");

        //Enabling Options
        enableOptions();
        
        //Load First Question
        loadQuestion(1);

    }

    private void loadQuestion(int quesNum) {

        //Set Question Number
        questionNumber.setText(quesNum);

        //Load Question Text
        questionText.setText(questionsToAnswer.get(quesNum-1).getQuestion());

        //Load Options
        optionOneBtn.setText(questionsToAnswer.get(quesNum-1).getOption_a());
        optionTwoBtn.setText(questionsToAnswer.get(quesNum-1).getOption_b());
        optionThreeBtn.setText(questionsToAnswer.get(quesNum-1).getOption_c());


        //Question Loaded, Set Can Answer
        canAnswer = true;
        currentQuestion = quesNum;

        //Start Question Timer
        startTimer(quesNum);

    }

    private void startTimer(int questionNumber) {

        //Set Timer Text
        final Long timeToAnswer = questionsToAnswer.get(questionNumber-1).getTimer();
        questionTime.setText(timeToAnswer.toString());

        //Show Timer Progressbar
        questionProgress.setVisibility(View.VISIBLE);

        //Start Count down timer class
        countDownTimer = new CountDownTimer(timeToAnswer*1000, 10){

            @Override
            public void onTick(long millisUntilFinished) {
                //Update UI
                questionTime.setText(millisUntilFinished/1000 + "");

                //Progress in Percent
                Long percent = millisUntilFinished/(timeToAnswer*10);
                questionProgress.setProgress(percent.intValue());
            }

            @Override
            public void onFinish() {
                //Time up, Cannot Answer
                canAnswer = false;
            }
        };

        countDownTimer.start();
    }

    private void enableOptions() {
        optionOneBtn.setVisibility(View.VISIBLE);
        optionTwoBtn.setVisibility(View.VISIBLE);
        optionThreeBtn.setVisibility(View.VISIBLE);

        //Enable Btn
        optionOneBtn.setEnabled(true);
        optionTwoBtn.setEnabled(true);
        optionThreeBtn.setEnabled(true);

        //Hide Feedback Text and Next Btn
        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.quiz_option_one:
                verifyAnswer(optionOneBtn);
                break;

            case R.id.quiz_option_two:
                verifyAnswer(optionTwoBtn);
                break;

            case R.id.quiz_option_three:
                verifyAnswer(optionThreeBtn);
                break;

            case R.id.quiz_next_btn:
                currentQuestion++;
                loadQuestion(currentQuestion);
                resetOptions();
                break;
        }
    }

    private void resetOptions() {
        optionOneBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));
        optionTwoBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));
        optionThreeBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));


        optionOneBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionTwoBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionThreeBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void verifyAnswer(Button selectedAnswer) {
        //Set Answer Text Color
        selectedAnswer.setTextColor(getResources().getColor(R.color.colorDark, null));

        //Check Answer
        if (canAnswer){
            if (questionsToAnswer.get(currentQuestion-1).getAnswer().equals(selectedAnswer.getText())){
                //Correct Answer
                correctAnswers++;
                selectedAnswer.setBackground(getResources().getDrawable(R.drawable.correct_answer_btn_bg, null));

                //Set Feedback Text
                questionFeedback.setText("Correct Answer");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            }else {
                //Wrong Answer
                wrongAnswers++;
                selectedAnswer.setBackground(getResources().getDrawable(R.drawable.wrong_answer_btn_bg, null));


                //Set Feedback Text
                questionFeedback.setText("Wrong Answer \n\n Correct Answer : " +
                        questionsToAnswer.get(currentQuestion-1).getAnswer());
                questionFeedback.setTextColor(getResources().getColor(R.color.colorAccent, null));

            }
            //Set can answer to false
            canAnswer = false;

            //Stop the Timer
            countDownTimer.cancel();

            //Show Next Button
            showNextBtn();

        }
    }

    private void showNextBtn() {
        questionFeedback.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(true);
    }
}
