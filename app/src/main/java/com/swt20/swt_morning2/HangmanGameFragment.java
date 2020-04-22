package com.swt20.swt_morning2;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static androidx.core.content.ContextCompat.getSystemService;

public class HangmanGameFragment extends Fragment {
    private static final String TAG = "[HangmanGameFragment]";
    int turn = 0;
    Map<Integer, Boolean> field_set_ = new HashMap<>();
    private InputMethodManager imgr;
    private TextView nextChar;
    private String word2guess;
    private String word2guess_viewtext = "";
    private TextView textViewWord2Guess;
    private WordList wordList;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hangman_game, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nextChar = view.findViewById(R.id.plainText_nextChar);
        imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        textViewWord2Guess = view.findViewById(R.id.textView_word2guess);
        //TODO read from shared prefs rewrite imput loading to load from shared prefs
        //TODO de und ed support for words: add en und de words in 2 jsons deppending on lang settings
        //TODO TCs

        String json = "";
        try {
            InputStream is = getContext().getAssets().open("words.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        }

        Gson gson = new Gson();
        wordList = gson.fromJson(json, WordList.class);

        word2guess = wordList.getRandomWord().toUpperCase();
        for (int i = 0; i < word2guess.length(); i++) {
            word2guess_viewtext += "_ ";
        }
        textViewWord2Guess.setText(word2guess_viewtext);

        nextChar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked");
            }
        });

        nextChar.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                imgr.hideSoftInputFromWindow(nextChar.getWindowToken(),0);
                String chrs = nextChar.getText().toString().toUpperCase();
                if (chrs.isEmpty()) {
                    Log.i(TAG, "chrs empty");
                    return;
                }
                char chr = chrs.charAt(0);
                Log.i(TAG, "" + (int) chr);
                if (chr == ' ') {
                    return;
                }
                if (word2guess.contains(Character.toString(chr))) {
                    int idx = word2guess.indexOf(chr);
                    StringBuilder newText = new StringBuilder(word2guess_viewtext);
                    while (idx >= 0) {
                        newText.setCharAt(idx * 2, chr);
                        idx = word2guess.indexOf(chr, idx + 1);
                    }
                    word2guess_viewtext = newText.toString();
                    textViewWord2Guess.setText(word2guess_viewtext);
                }
                nextChar.setText("");

                String result = word2guess_viewtext.replace(" ", "");
                if (word2guess.equalsIgnoreCase(result)) {
                    ScoreTracker st = new ScoreTracker(getContext());
                    st.addScore(Game.HANGMAN, 1);
                    Toast.makeText(getContext(), getString(R.string.hangman_WIN) + " (" + word2guess + ")", Toast.LENGTH_LONG).show();

                    word2guess = wordList.getRandomWord().toUpperCase();
                    word2guess_viewtext = "";
                    for (int i = 0; i < word2guess.length(); i++) {
                        word2guess_viewtext += "_ ";
                    }
                    textViewWord2Guess.setText(word2guess_viewtext);
                }
            }
        });

        nextChar.requestFocus();
        imgr.showSoftInput(nextChar, InputMethodManager.SHOW_IMPLICIT);
    }

    public class WordList {
        public List<String> standardWords;
        public List<String> customWords;

        public String getRandomWord() {
            List<String> tmpList = new ArrayList<String>();
            tmpList.addAll(standardWords);
            tmpList.addAll(customWords);
            Random rand = new Random();
            return tmpList.get(rand.nextInt(tmpList.size()));
        }
    }
}
