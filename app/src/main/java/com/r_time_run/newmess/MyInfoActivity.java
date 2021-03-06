package com.r_time_run.newmess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.r_time_run.newmess.constant.Constant;
import com.r_time_run.newmess.net.NMParameters;
import com.r_time_run.newmess.subactivity.BackOrder;
import com.r_time_run.newmess.subactivity.BagOrder;
import com.r_time_run.newmess.subactivity.BaseActivity;
import com.r_time_run.newmess.subactivity.ChangePass;
import com.r_time_run.newmess.subactivity.EvaluateOrder;
import com.r_time_run.newmess.subactivity.MyAllInfo;
import com.r_time_run.newmess.subactivity.MyBuyed;
import com.r_time_run.newmess.subactivity.MyCollection;
import com.r_time_run.newmess.subactivity.RegisteActivity;
import com.rey.material.widget.CheckBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MyInfoActivity extends BaseActivity implements View.OnClickListener {


    private TextView bt_bagOrder, bt_bagAppraise, bt_backBag, bt_goodFood, bt_buyedFood,
            bt_personInfo, bt_changePassword, bt_backLogin, tv_user_type, bt_registe_new_user;
    private Button bt_user_land,bt_login_text;
    private View v_left;
    private ProgressBarCircularIndeterminate pb_wait_login;
    private LinearLayout ll_login_content,ll_wait_login;
    private SharedPreferences sp;       //下次打开时显示上次的账号和密码
    public static Boolean registeToFirst = false;
    public static Boolean isLogined = false;       //判断是否已经登录
    private boolean isLoginState;            //返回的登录状态码

    private EditText et_user_name, et_user_password;
    private CheckBox cb_remery_user_password;
    private DrawerLayout drawerLayout;
    private SQLiteDatabase db;
    private LinearLayout ll_my_info_student, ll_my_info_shangjia, ll_my_info;
    private ImageView ib_myinfo_touxiang;
    private String loginType = "学生";
    private static Boolean isExit = false;

    private PropertyAction leftAction;
    private PropertyAction loginTextAction;
    private PropertyAction progressAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        initActivity();     //初始化我的信息界面
        initLogin();        //初次进入时的登录界面
        setLoginEt();           //从注册页面进入主页面时所做的事情

        getDataFromServlet();       //将过期的订单转入到评价订单和买过的订单中
    }

    /**
     * 初始化我的信息界面
     */
    private void initActivity() {
        ll_my_info = (LinearLayout) findViewById(R.id.ll_my_info);
        ll_my_info_student = (LinearLayout) findViewById(R.id.ll_my_info_student);
        ll_my_info_student.setVisibility(View.GONE);
        ll_my_info_shangjia = (LinearLayout) findViewById(R.id.ll_my_info_shangjia);
        ll_my_info_shangjia.setVisibility(View.GONE);
        ll_wait_login = (LinearLayout) findViewById(R.id.ll_wait_login);
        ll_wait_login.setVisibility(View.GONE);
        bt_login_text = (Button) findViewById(R.id.bt_login_text);
        v_left = findViewById(R.id.v_left);
        pb_wait_login = (ProgressBarCircularIndeterminate) findViewById(R.id.pb_wait_login);
        ll_login_content = (LinearLayout) findViewById(R.id.ll_login_content);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        bt_bagOrder = (TextView) findViewById(R.id.bt_bagOrder);
        bt_bagAppraise = (TextView) findViewById(R.id.bt_bagAppraise);
        bt_backBag = (TextView) findViewById(R.id.bt_backBag);
        bt_goodFood = (TextView) findViewById(R.id.bt_goodFood);
        bt_buyedFood = (TextView) findViewById(R.id.bt_buyedFood);
        bt_personInfo = (TextView) findViewById(R.id.bt_personInfo);
        bt_changePassword = (TextView) findViewById(R.id.bt_changePassword);
        bt_backLogin = (TextView) findViewById(R.id.bt_backLogin);
        ib_myinfo_touxiang = (ImageView) findViewById(R.id.ib_myinfo_touxiang);
        tv_user_type = (TextView) findViewById(R.id.tv_user_type);
        bt_registe_new_user = (TextView) findViewById(R.id.bt_user_registe);
        bt_user_land = (Button) findViewById(R.id.bt_user_login);
        db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString() + "/order_message_db.db3", null);

        //初始化设置头像
        Bitmap touxiang = BitmapFactory.decodeFile(sp.getString("picPath", ""));
        if (touxiang != null) {
            ib_myinfo_touxiang.setImageBitmap(touxiang);
        }

        bt_bagOrder.setOnClickListener(this);
        bt_bagAppraise.setOnClickListener(this);
        ib_myinfo_touxiang.setOnClickListener(this);
        bt_backBag.setOnClickListener(this);
        bt_goodFood.setOnClickListener(this);
        bt_buyedFood.setOnClickListener(this);
        bt_personInfo.setOnClickListener(this);
        bt_changePassword.setOnClickListener(this);
        bt_backLogin.setOnClickListener(this);
        bt_registe_new_user.setOnClickListener(this);
        bt_user_land.setOnClickListener(this);
        bt_login_text.setOnClickListener(this);
    }

    /**
     * 初次进入时的登录界面
     */
    private void initLogin() {
        et_user_password = (EditText) findViewById(R.id.et_user_password);
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        et_user_name.addTextChangedListener(new NameChangeListener());
        cb_remery_user_password = (CheckBox) findViewById(R.id.cb_remory_user_password);
        //载入该页面时判断上次是否记住密码并为账号和密码的EditText填写相应的内容
        et_user_name.setText(sp.getString("username", ""));
        et_user_password.setText(sp.getString("userpassword", ""));
        cb_remery_user_password.setChecked(sp.getBoolean("remorypassword", false));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //说明：点击“我的”按钮之后，若是没有登录，则进入登录界面，而若是已登录，则进入“我的信息”界面
        isLogined = sp.getBoolean("islogined", false);
        if (isLogined) {
            //若是已经登录过的，则直接在主界面而不再出现登录界面
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            ll_login_content.setVisibility(View.GONE);
            ll_wait_login.setVisibility(View.VISIBLE);
            judge();
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            ll_login_content.setVisibility(View.VISIBLE);
            ll_wait_login.setVisibility(View.GONE);
        }
    }

    /**
     * 若是从注册页面返回主页，则为自动为登录界面的账号和密码填写内容
     */
    public void setLoginEt() {
        if (registeToFirst) {
            Intent intent = getIntent();
            Bundle data = intent.getExtras();
            String name = data.getString("user_name");
            et_user_name.setText(name);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        }
    }

    /**
     * 对账户密码进行判断后是否进入我的信息页面
     */
    public void judge() {
        //点击登陆后进入等待登陆界面
        ll_wait_login.setVisibility(View.VISIBLE);
        ll_login_content.setVisibility(View.GONE);
        //点击登陆进入等待登陆完成的界面
        leftAction = PropertyAction.newPropertyAction(v_left).
                interpolator(new LinearInterpolator()).
                translationX(-300).
                duration(550).
                build();
        loginTextAction = PropertyAction.newPropertyAction(bt_login_text).
                interpolator(new LinearInterpolator()).
                duration(1050).
                alpha(0.4f).
                build();
        progressAction = PropertyAction.newPropertyAction(pb_wait_login).
                interpolator(new LinearInterpolator()).
                alpha(0f).
                duration(550).
                build();
        Player.init().
                animate(leftAction).
                then().
                animate(loginTextAction).
                then().
                animate(progressAction).
                play();
        //请求服务器进行登陆
        final NMParameters loginParames = new NMParameters();
        loginParames.add("action", "login");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_number", et_user_name.getText().toString());
            jsonObject.put("user_password", et_user_password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loginParames.add("login", jsonObject.toString());

        getData(TAG_LOGIN, Constant.URL_FOODS_TEXT, loginParames, "POST");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_bagOrder:      //订单详情
                startActivity(new Intent(MyInfoActivity.this, BagOrder.class));
                break;
            case R.id.bt_bagAppraise:       //评价订单
                startActivity(new Intent(MyInfoActivity.this, EvaluateOrder.class));
                break;
            case R.id.bt_backBag:       //退订
                startActivity(new Intent(MyInfoActivity.this, BackOrder.class));
                break;
            case R.id.bt_goodFood:      //已收藏食品
                startActivity(new Intent(MyInfoActivity.this, MyCollection.class));
                break;
            case R.id.bt_buyedFood:     //已购买食品
                startActivity(new Intent(MyInfoActivity.this, MyBuyed.class));
                break;
            case R.id.bt_personInfo:        //我的详细信息
                startActivity(new Intent(MyInfoActivity.this, MyAllInfo.class));
                break;
            case R.id.bt_changePassword:        //修改密码
                startActivityForResult(new Intent(MyInfoActivity.this, ChangePass.class), 0);
                break;
            case R.id.bt_backLogin:     //退出登录按钮
                MyInfoActivity.isLogined = false;
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("islogined", isLogined);
                editor.commit();
                startActivity(new Intent(MyInfoActivity.this, FirstActivity.class));
                MyInfoActivity.this.finish();
                break;
            case R.id.bt_user_registe:      //注册按钮
                Intent intent = new Intent(MyInfoActivity.this, RegisteActivity.class);
                startActivity(intent);
                MyInfoActivity.this.finish();
                overridePendingTransition(R.anim.my_info_in, R.anim.my_info_out);
                break;
            case R.id.bt_user_login:        //登录按钮
                judge();
                break;
            case R.id.bt_login_text:        //停止登录
                ll_wait_login.setVisibility(View.GONE);
                ll_login_content.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor1 = sp.edit();
                editor1.putBoolean("islogined", false);      //是否已登录
                editor1.commit();
                break;
        }
    }

    /**
     * 当用户名输入框删除输入时响应：
     * 既删除用户名时顺便删除密码
     */
    private class NameChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            et_user_password.setText("");
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    /**
     * 是否记住密码
     */
    private void remeryUserPassword() {
        SharedPreferences.Editor editor = sp.edit();
        //保存账号，下次启动时使用
        editor.putString("username", et_user_name.getText().toString());
        //判断是否保存密码，下次启动时使用
        Boolean status = cb_remery_user_password.isChecked();
        editor.putString("userpassword", "");
        if (status) {
            editor.putString("userpassword", et_user_password.getText().toString());
        }
        //保存是否记住密码的状态
        editor.putBoolean("remorypassword", cb_remery_user_password.isChecked());
        editor.putBoolean("islogined", isLogined);      //是否已登录
        editor.commit();
    }

    @Override
    public void handleMsg(Message msg) {
        Bundle data = msg.getData();
        String json = data.getString("json");
        System.out.println("!!!!" + json);
        System.out.println("!!!!" + msg.what);
        try {
            JSONObject obj = new JSONObject(json);
            if (msg.what == TAG_LOGIN) {
                isLoginState = obj.getBoolean("state");
                if (isLoginState) {
                    ll_login_content.setVisibility(View.VISIBLE);
                    isLogined = true;       //登录成功
                    remeryUserPassword();               //是否记住密码
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                            if (loginType.equals("学生")) {
                                //进入学生界面
                                ll_my_info_student.setVisibility(View.VISIBLE);
                                ll_my_info.invalidate();
                                tv_user_type.setText("student");
                            } else if (loginType.equals("商家")) {
                                //进入商家界面
                                ll_my_info_shangjia.setVisibility(View.VISIBLE);
                                ll_my_info.invalidate();
                                tv_user_type.setText("shopman");
                            }
                        }
                    }, 300);
                } else{
                    Log.e("MyInfoActivity","密码错误");
                    Toast.makeText(MyInfoActivity.this,"密码错误",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
        }
        return true;
    }

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        //初始化设置头像
        Bitmap touxiang = BitmapFactory.decodeFile(sp.getString("picPath", ""));
        if (touxiang != null) {
            ib_myinfo_touxiang.setImageBitmap(touxiang);
        }
    }

    /**
     * 将过期的订单转入到评价订单和买过的订单中
     */
    private void getDataFromServlet() {
        try {
            Cursor cursor = db.rawQuery("select * from food_info", null);
            while (cursor.moveToNext()) {
                if (Integer.parseInt(cursor.getString(6)) != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)){
                    setSaveEvaluateData(cursor);
                    setSaveBuyedData(cursor);
                    db.execSQL("delete from food_info where name=? and day=? and hour=? and minute=?",
                            new String[]{cursor.getString(0),cursor.getString(6),cursor.getString(7),cursor.getString(8)});
                }
            }

        } catch (SQLiteException se) {

        }
    }

    /**
     * 保存评价订单的数据
     * 使用SQLite来保存
     */
    private void setSaveEvaluateData(Cursor cursor) {
        String sqlCreatTable = "create table food_evaluate(name varchar(50),adress varchar(100)," +
                "number varchar(10),price varchar(10),year varchar(50),month varchar(50)" +
                ",day varchar(50),hour varchar(50),minute varchar(50))";

        String foodName = cursor.getString(0);
        String foodLocation = cursor.getString(1);
        String foodNum = cursor.getString(2);
        String foodPrice = cursor.getString(3);
        String year = cursor.getString(4);
        String month = cursor.getString(5);
        String day = cursor.getString(6);
        String hour = cursor.getString(7);
        String minute = cursor.getString(8);

        try {
            insertEvaluateData(db, foodName, foodLocation, foodNum, foodPrice, year, month, day, hour, minute);
        } catch (SQLiteException se) {
            db.execSQL(sqlCreatTable);
            insertEvaluateData(db, foodName, foodLocation, foodNum, foodPrice, year, month, day, hour, minute);
        }
    }

    private void insertEvaluateData(SQLiteDatabase db, String foodName, String foodLocation, String foodNum, String foodPrice, String year, String month, String day, String hour, String minute) {
        db.execSQL("insert into food_evaluate values(?,?,?,?,?,?,?,?,?)", new String[]{foodName, foodLocation, foodNum, foodPrice, year + "", month + "", day + "", hour + "", minute + ""});
    }

    /**
     * 保存已买过的食品数据
     * 使用SQLite来保存
     */
    private void setSaveBuyedData(Cursor cursor) {
        String sqlCreatTable = "create table food_buyed(name varchar(50),adress varchar(100)," +
                "number varchar(10),price varchar(10),year varchar(50),month varchar(50)" +
                ",day varchar(50),hour varchar(50),minute varchar(50))";

        String foodName = cursor.getString(0);
        String foodLocation = cursor.getString(1);
        String foodNum = cursor.getString(2);
        String foodPrice = cursor.getString(3);
        String year = cursor.getString(4);
        String month = cursor.getString(5);
        String day = cursor.getString(6);
        String hour = cursor.getString(7);
        String minute = cursor.getString(8);

        try {
            insertBuyedData(db, foodName, foodLocation, foodNum, foodPrice, year, month, day, hour, minute);
        } catch (SQLiteException se) {
            db.execSQL(sqlCreatTable);
            insertBuyedData(db, foodName, foodLocation, foodNum, foodPrice, year, month, day, hour, minute);
        }
    }

    private void insertBuyedData(SQLiteDatabase db, String foodName, String foodLocation, String foodNum, String foodPrice, String year, String month, String day, String hour, String minute) {
        db.execSQL("insert into food_buyed values(?,?,?,?,?,?,?,?,?)", new String[]{foodName, foodLocation, foodNum, foodPrice, year + "", month + "", day + "", hour + "", minute + ""});
    }
}
