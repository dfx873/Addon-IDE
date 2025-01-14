/*
 * Copyright 2018. who<980008027@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.jsdroid.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import java.security.Key;
import android.graphics.Rect;
import com.addon.json.Global;
import android.text.SpannableStringBuilder;
import bms.helper.tools.LOG;
import android.view.ViewTreeObserver;

/**
 * Created by Administrator on 2018/2/11.
 */

public class CodeText extends ColorsText {
    JsCodeParser codeParser;
    public CompletionMsg use;

    private int pWidth;

    private int pHeight;

    public void setPHeight(int pHeight) {
        this.pHeight = pHeight;
    }

    public int getPHeight() {
        return pHeight;
    }

    public void setPWidth(int pWidth) {
        this.pWidth = pWidth;
    }

    public int getPWidth() {
        return pWidth;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (use != null) {
            SpannableStringBuilder ssb=(SpannableStringBuilder) getText();
            int now=selEnd;
            StringBuilder sb=new StringBuilder();
            boolean choose=false;
            while (true) {
                if (now - 1 < 0 || now - 1 >= ssb.length())return;
                char c=ssb.charAt(now - 1);
                if (c == '"') {
                    choose = true;
                }
                if (('0' <= c && c <= '9') || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c == '_' || c == ':' || c == '.') {
                    sb.insert(0, c);
                } else {
                    break;
                }

                now--;
            }
            //LOG.print("",sb.toString());
            if (sb.length() > 0 || choose) {
                use.change(sb.toString());
            } else {
                use.close();
            }
        }
    }

    public void setCheck(GrammarCheck.Check c) {
        super.check = c;
    }

    @Override
    protected int getParentWidth() {
        return pWidth;
    }

    @Override
    protected int getParentHeight() {
        return pHeight;
    }
    
    
    

    public CodeText(Context context) {
        super(context);
        init();
    }

    public CodeText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        codeParser = new JsCodeParser(this);
        // 动态解析js代码更新文字颜色

        addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    //卡顿罪魁祸首之一
                    codeParser.parse(start, before, count);

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        
        
    }

    private long drawUseTimeCount = 0;
    private long lastEnterTime = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        
        super.onDraw(canvas);
        
    }

    /**
     * 记录用户操作的键盘，避免一次按键，多次输入
     */
    private int defaultDeviceId = -1000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int deviceId = event.getDeviceId();
        if (defaultDeviceId == -1000) {
            defaultDeviceId = deviceId;
        } else {
            if (defaultDeviceId == -1) {
                if (deviceId != defaultDeviceId) {
                    defaultDeviceId = deviceId;
                    return true;
                }
            }
        }

        if (deviceId != defaultDeviceId) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_TAB) {
            int start = getSelectionStart();
            int end = getSelectionEnd();
            if (start == end) {
                getText().insert(start, "  ");
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            lastEnterTime = System.currentTimeMillis();
            Layout layout = getLayout();
            if (layout == null) {
                return true;
            }
            int start = getSelectionStart();
            int end = getSelectionEnd();
            if (start == end) {
                int line = layout.getLineForOffset(start);
                int startPos = layout.getLineStart(line);
                String space = "";
                for (int i = startPos; i < getText().length(); i++) {
                    if (getText().charAt(i) == ' ') {
                        space += " ";
                    } else {
                        break;
                    }
                }
                getText().insert(start, "\n" + space);
                if (start > 0) {
                    char c = getText().charAt(start - 1);
                    if (c == '{' || c == '[' || c == '(') {
                        start = getSelectionStart();
                        getText().insert(start, "  ");
                        start = getSelectionStart();
                        getText().insert(start, "\n" + space);
                        setSelection(start);
                    }
                }

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static interface CompletionMsg {
        void change(String input);
        void close();
    }

}
