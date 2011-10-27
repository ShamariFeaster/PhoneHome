package edu.fsu.ed.feaster;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Dialogue extends Dialog implements android.view.View.OnClickListener {

	Button ConfirmBtn;
	TextView UserMessage;
	TextView DialogueTitle;
	
	SharedPreferences preferences;
	
	public Dialogue(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		preferences = context.getSharedPreferences("preferences", Context.MODE_WORLD_READABLE);
		setContentView(R.layout.dialogue_layout);
		UserMessage = (TextView) findViewById(R.id.UserMessage);
		UserMessage.setText(preferences.getString("message", "Failed!"));
		ConfirmBtn = (Button) findViewById(R.id.ConfirmBtn);
		ConfirmBtn.setOnClickListener(this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == ConfirmBtn) {
			dismiss();
		}
	}

}
