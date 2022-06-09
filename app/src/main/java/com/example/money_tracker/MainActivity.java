package com.example.money_tracker;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    public String date;
    MenuItem  sortTime, sortDate ,sortAmnt;
    TextView tvSign ;
    public static TextView tvEmpty, tvBalance;
    EditText etAmount, etMessage;
    ImageView ivSend, ivCal;
    boolean positive = true;
    boolean stateStatusDate = true;
    boolean stateStatusAmnt = true;

    RecyclerView rvTransactions;

    TransactionAdapter adapter;
    ArrayList<TransactionClass> transactionList;

    // On create method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Function to initialize views
        initViews();

        // Function to load data from shared preferences
        loadData();

        // Function to set custom action bar
        setCustomActionBar();

        // To check if there is no transaction
        checkIfEmpty(transactionList.size());

        // Initializing recycler view
        rvTransactions.setHasFixedSize(true);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this,transactionList);
        rvTransactions.setAdapter(adapter);
        tvSign.setBackgroundResource(R.drawable.ic_up);

        // On click sign change
        tvSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSign();
            }
        });
        ivCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDatePickerDailog();
            }
        });


        // On click Send
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // Input Validation
                if(etAmount.getText().toString().trim().isEmpty())
                {
                    etAmount.setError("Enter Amount!");
                    return;
                }
                if(etMessage.getText().toString().isEmpty())
                {
                    etMessage.setError("Enter a message!");
                    return;
                }

                try {
                    int amt = Integer.parseInt(etAmount.getText().toString().trim());;
                    //if date not selected put current date
                    if(date == null){
                        Date Date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
                        date = formatter.format(Date);
                    }
                    sendTransaction(amt,date,etMessage.getText().toString().trim(),positive);
                    checkIfEmpty(transactionList.size());

                    // To update Balance
                    setBalance(transactionList);
                    etAmount.setText("");
                    etMessage.setText("");
                }
                catch (Exception e){
                    etAmount.setError("Amount should be integer greater than zero!");
                }
            }
        });
    }
    //creating the drop down menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drop_menu, menu);
        return true;

    }
    //When the menu items are clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        Toast toast= Toast.makeText(getApplicationContext(), "Your string here", Toast.LENGTH_SHORT);
        //custom toast notification doesn't work on andorid 12
        //View view = toast.getView();
        //TextView text = view.findViewById(android.R.id.message);
        //text.setTextColor(Color.WHITE);
        //view.getBackground().setColorFilter(Color.parseColor("#FF3700B3"), PorterDuff.Mode.SRC_IN);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 250);

        String amount1 = "";
        String amount2 = "";


        switch (item.getItemId())
        {

            case R.id.sortDate:
                if(stateStatusDate){
                    Collections.sort(transactionList,new Comparator<TransactionClass>(){
                        @Override
                        public int compare(TransactionClass o1, TransactionClass o2) {
                            return o1.date.compareToIgnoreCase(o2.date);
                        }
                    });
                    stateStatusDate = false;
                    adapter.notifyDataSetChanged();
                    toast.setText("Sort by Date Ascending");
                    toast.show();
                }else{
                    Collections.reverse(transactionList);
                    stateStatusDate = true;
                    adapter.notifyDataSetChanged();
                    toast.setText("Sort by Date Descending");
                    toast.show();
                }
                break;
            case R.id.sortAmount:
                if(stateStatusAmnt){
                    Collections.sort(transactionList,new Sortbyroll());
                    adapter.notifyDataSetChanged();
                    stateStatusAmnt = false;
                    toast.setText("Sort by Amount Ascending");
                    toast.show();
                } else{
                    Collections.reverse(transactionList);
                    adapter.notifyDataSetChanged();
                    stateStatusAmnt = true;
                    toast.setText("Sort by Amount Descending");
                    toast.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // To set custom action bar
    private void setCustomActionBar() {
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        View v = LayoutInflater.from(this).inflate(R.layout.custom_action_bar,null);

        // TextView to show Balance
        tvBalance = v.findViewById(R.id.tvBalance);

        // Setting balance
        setBalance(transactionList);
        getSupportActionBar().setCustomView(v);
        getSupportActionBar().setElevation(0);
    }

    // To set Balance along with sign (spent(-) or received(+))
    public static void setBalance(ArrayList<TransactionClass> transactionList){
        int bal = calculateBalance(transactionList);
        if(bal<0)
        {
            tvBalance.setText("-"+calculateBalance(transactionList)*-1);
        }
        else {
            tvBalance.setText("+"+calculateBalance(transactionList));
        }
    }

    // To load data from shared preference
    private void loadData() {
        SharedPreferences pref = getSharedPreferences("com.cs.ec",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("transactions",null);
        Type type = new TypeToken<ArrayList<TransactionClass>>(){}.getType();
        if(json!=null)
        {
            transactionList=gson.fromJson(json,type);
        }
    }

    // To add transaction
    private void sendTransaction(int amt,String dte,String msg, boolean positive) {
        transactionList.add(new TransactionClass(amt,dte,msg,positive));
        adapter.notifyDataSetChanged();
        rvTransactions.smoothScrollToPosition(transactionList.size()-1);
    }

    // Function to change sign
    private void changeSign() {
        if(positive)
        {
            tvSign.setBackgroundResource(R.drawable.ic_down);
            tvSign.setTextColor(Color.parseColor("#F44336"));
            positive = false;
        }
        else {
            tvSign.setBackgroundResource(R.drawable.ic_up);
            tvSign.setTextColor(Color.parseColor("#00c853"));
            positive = true;
        }
    }

    // To check if transaction list is empty
    public static void checkIfEmpty(int size) {
        if (size == 0)
        {
            MainActivity.tvEmpty.setVisibility(View.VISIBLE);
        }
        else {
            MainActivity.tvEmpty.setVisibility(View.GONE);
        }
    }

    // To Calculate Balance by iterating through all transactions
    public static int calculateBalance(ArrayList<TransactionClass> transactionList)
    {
        int bal = 0;
        for(TransactionClass transaction : transactionList)
        {
            if(transaction.isPositive())
            {
                bal+=transaction.getAmount();
            }
            else {
                bal-=transaction.getAmount();
            }
        }
        return bal;
    }

    // Initializing Views
    private void initViews() {
        transactionList = new ArrayList<TransactionClass>();
        tvSign = findViewById(R.id.tvSign);
        rvTransactions = findViewById(R.id.rvTransactions);
        etAmount = findViewById(R.id.etAmount);
        etMessage = findViewById(R.id.etMessage);
        ivCal = findViewById(R.id.ivCal);
        ivSend = findViewById(R.id.ivSend);
        tvEmpty = findViewById(R.id.tvEmpty);
        ///sorts
        sortDate =  findViewById(R.id.sortDate);
        sortAmnt =  findViewById(R.id.sortAmount);
    }


    // Storing data locally
    // using shared preferences
    // in onStop() method
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getSharedPreferences("com.cs.ec",MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(transactionList);
        editor.putString("transactions",json);
        editor.apply();
    }
    public void ShowDatePickerDailog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        month+=1;
        date = year+"-"+month+"-"+dayOfMonth;
    }
}