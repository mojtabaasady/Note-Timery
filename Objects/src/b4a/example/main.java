package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;
    public static boolean dontPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create " + (isFirst ? "(first time)" : "") + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        if (!dontPause)
            BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        else
            BA.LogInfo("** Activity (main) Pause event (activity is not paused). **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        if (!dontPause) {
            processBA.setActivityPaused(true);
            mostCurrent = null;
        }

        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.B4XViewWrapper.XUI _v0 = null;
public static String _v6 = "";
public static String _v7 = "";
public anywheresoftware.b4a.objects.LabelWrapper _label_addnewnote = null;
public anywheresoftware.b4a.objects.LabelWrapper _label_clearnewnote = null;
public anywheresoftware.b4a.objects.LabelWrapper _label_closenewnote = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext_note = null;
public anywheresoftware.b4a.objects.ListViewWrapper _listview_note = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext_timer_hh = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext_timer_mm = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext_date_mm = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext_date_dd = null;
public b4a.example.starter _vv1 = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 35;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 36;BA.debugLine="Activity.LoadLayout(\"Layout_Main\")";
mostCurrent._activity.LoadLayout("Layout_Main",mostCurrent.activityBA);
 //BA.debugLineNum = 37;BA.debugLine="StartApp";
_v5();
 //BA.debugLineNum = 38;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 44;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 46;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 40;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 42;BA.debugLine="End Sub";
return "";
}
public static String  _edittext_date_dd_textchanged(String _old,String _new) throws Exception{
 //BA.debugLineNum = 106;BA.debugLine="Private Sub EditText_Date_DD_TextChanged (Old As S";
 //BA.debugLineNum = 108;BA.debugLine="End Sub";
return "";
}
public static String  _edittext_date_mm_textchanged(String _old,String _new) throws Exception{
 //BA.debugLineNum = 110;BA.debugLine="Private Sub EditText_Date_MM_TextChanged (Old As S";
 //BA.debugLineNum = 112;BA.debugLine="End Sub";
return "";
}
public static String  _edittext_note_textchanged(String _old,String _new) throws Exception{
 //BA.debugLineNum = 82;BA.debugLine="Private Sub EditText_Note_TextChanged (Old As Stri";
 //BA.debugLineNum = 84;BA.debugLine="End Sub";
return "";
}
public static String  _edittext_timer_hh_textchanged(String _old,String _new) throws Exception{
 //BA.debugLineNum = 118;BA.debugLine="Private Sub EditText_Timer_HH_TextChanged (Old As";
 //BA.debugLineNum = 120;BA.debugLine="End Sub";
return "";
}
public static String  _edittext_timer_mm_textchanged(String _old,String _new) throws Exception{
 //BA.debugLineNum = 114;BA.debugLine="Private Sub EditText_Timer_MM_TextChanged (Old As";
 //BA.debugLineNum = 116;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 21;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 23;BA.debugLine="Dim Text1,Text2 As String";
mostCurrent._v6 = "";
mostCurrent._v7 = "";
 //BA.debugLineNum = 24;BA.debugLine="Private Label_AddNewNote As Label";
mostCurrent._label_addnewnote = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Private Label_ClearNewNote As Label";
mostCurrent._label_clearnewnote = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Private Label_CloseNewNote As Label";
mostCurrent._label_closenewnote = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private EditText_Note As EditText";
mostCurrent._edittext_note = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private ListView_Note As ListView";
mostCurrent._listview_note = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private EditText_Timer_HH As EditText";
mostCurrent._edittext_timer_hh = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private EditText_Timer_MM As EditText";
mostCurrent._edittext_timer_mm = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private EditText_Date_MM As EditText";
mostCurrent._edittext_date_mm = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private EditText_Date_DD As EditText";
mostCurrent._edittext_date_dd = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 33;BA.debugLine="End Sub";
return "";
}
public static String  _label_addnewnote_click() throws Exception{
 //BA.debugLineNum = 57;BA.debugLine="Private Sub Label_AddNewNote_Click";
 //BA.debugLineNum = 58;BA.debugLine="Text1=EditText_Note.Text";
mostCurrent._v6 = mostCurrent._edittext_note.getText();
 //BA.debugLineNum = 59;BA.debugLine="Text2=\"Time : \"&EditText_Timer_HH.text&\":\"&EditTe";
mostCurrent._v7 = "Time : "+mostCurrent._edittext_timer_hh.getText()+":"+mostCurrent._edittext_timer_mm.getText()+" Date : "+mostCurrent._edittext_date_mm.getText()+"/"+mostCurrent._edittext_date_dd.getText();
 //BA.debugLineNum = 60;BA.debugLine="ListView_Note.AddSingleLine(\"New Timer\")";
mostCurrent._listview_note.AddSingleLine(BA.ObjectToCharSequence("New Timer"));
 //BA.debugLineNum = 61;BA.debugLine="ListView_Note.AddTwoLinesAndBitmap(Text1, Text2,";
mostCurrent._listview_note.AddTwoLinesAndBitmap(BA.ObjectToCharSequence(mostCurrent._v6),BA.ObjectToCharSequence(mostCurrent._v7),(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"icon.png").getObject()));
 //BA.debugLineNum = 62;BA.debugLine="End Sub";
return "";
}
public static String  _label_clearnewnote_click() throws Exception{
 //BA.debugLineNum = 68;BA.debugLine="Private Sub Label_ClearNewNote_Click";
 //BA.debugLineNum = 70;BA.debugLine="End Sub";
return "";
}
public static String  _label_closenewnote_click() throws Exception{
 //BA.debugLineNum = 64;BA.debugLine="Private Sub Label_CloseNewNote_Click";
 //BA.debugLineNum = 66;BA.debugLine="End Sub";
return "";
}
public static String  _listview_note_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 74;BA.debugLine="Private Sub ListView_Note_ItemClick (Position As I";
 //BA.debugLineNum = 76;BA.debugLine="End Sub";
return "";
}
public static String  _listview_note_itemlongclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 78;BA.debugLine="Private Sub ListView_Note_ItemLongClick (Position";
 //BA.debugLineNum = 80;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}

private static byte[][] bb;

public static String vvv13(final byte[] _b, final int i) throws Exception {
Runnable r = new Runnable() {
{

int value = i / 2 + 284299;
if (bb == null) {
		
                bb = new byte[4][];
				bb[0] = BA.packageName.getBytes("UTF8");
                bb[1] = BA.applicationContext.getPackageManager().getPackageInfo(BA.packageName, 0).versionName.getBytes("UTF8");
                if (bb[1].length == 0)
                    bb[1] = "jsdkfh".getBytes("UTF8");
                bb[2] = new byte[] { (byte)BA.applicationContext.getPackageManager().getPackageInfo(BA.packageName, 0).versionCode };			
        }
        bb[3] = new byte[] {
                    (byte) (value >>> 24),
						(byte) (value >>> 16),
						(byte) (value >>> 8),
						(byte) value};
				try {
					for (int __b = 0;__b < (3 + 1);__b ++) {
						for (int b = 0;b<_b.length;b++) {
							_b[b] ^= bb[__b][b % bb[__b].length];
						}
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
                

            
}
public void run() {
}
};
return new String(_b, "UTF8");
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Private xui As XUI";
_v0 = new anywheresoftware.b4a.objects.B4XViewWrapper.XUI();
 //BA.debugLineNum = 19;BA.debugLine="End Sub";
return "";
}
public static String  _spinner_date_dd_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 86;BA.debugLine="Private Sub Spinner_Date_DD_ItemClick (Position As";
 //BA.debugLineNum = 88;BA.debugLine="End Sub";
return "";
}
public static String  _spinner_date_mm_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 90;BA.debugLine="Private Sub Spinner_Date_MM_ItemClick (Position As";
 //BA.debugLineNum = 92;BA.debugLine="End Sub";
return "";
}
public static String  _spinner_date_yy_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 94;BA.debugLine="Private Sub Spinner_Date_YY_ItemClick (Position As";
 //BA.debugLineNum = 96;BA.debugLine="End Sub";
return "";
}
public static String  _spinner_timer_hh_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 102;BA.debugLine="Private Sub Spinner_Timer_HH_ItemClick (Position A";
 //BA.debugLineNum = 104;BA.debugLine="End Sub";
return "";
}
public static String  _spinner_timer_mm_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 98;BA.debugLine="Private Sub Spinner_Timer_MM_ItemClick (Position A";
 //BA.debugLineNum = 100;BA.debugLine="End Sub";
return "";
}
public static String  _v5() throws Exception{
 //BA.debugLineNum = 48;BA.debugLine="Sub StartApp";
 //BA.debugLineNum = 50;BA.debugLine="EditText_Timer_HH.text=DateTime.GetHour(DateTime.";
mostCurrent._edittext_timer_hh.setText(BA.ObjectToCharSequence(anywheresoftware.b4a.keywords.Common.DateTime.GetHour(anywheresoftware.b4a.keywords.Common.DateTime.getNow())));
 //BA.debugLineNum = 51;BA.debugLine="EditText_Timer_MM.text=DateTime.GetMinute(DateTim";
mostCurrent._edittext_timer_mm.setText(BA.ObjectToCharSequence(anywheresoftware.b4a.keywords.Common.DateTime.GetMinute(anywheresoftware.b4a.keywords.Common.DateTime.getNow())));
 //BA.debugLineNum = 52;BA.debugLine="EditText_Date_MM.text=DateTime.GetMonth(DateTime.";
mostCurrent._edittext_date_mm.setText(BA.ObjectToCharSequence(anywheresoftware.b4a.keywords.Common.DateTime.GetMonth(anywheresoftware.b4a.keywords.Common.DateTime.getNow())));
 //BA.debugLineNum = 53;BA.debugLine="EditText_Date_DD.text=DateTime.GetDayOfMonth(Date";
mostCurrent._edittext_date_dd.setText(BA.ObjectToCharSequence(anywheresoftware.b4a.keywords.Common.DateTime.GetDayOfMonth(anywheresoftware.b4a.keywords.Common.DateTime.getNow())));
 //BA.debugLineNum = 55;BA.debugLine="End Sub";
return "";
}
}
