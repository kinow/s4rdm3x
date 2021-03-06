package gui;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.*;
import imgui.font.FontGlyph;
import imgui.internal.ItemFlag;
import imgui.internal.classes.Rect;
import imgui.internal.classes.Window;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import static imgui.ImguiKt.COL32;

public class ImGuiWrapper {
    private ImGui m_imGui;

    static private int g_textBufferExtraSize = 256;
    private boolean m_isWidgetDisabled = false;

    public ImGui imgui() {
        return m_imGui;
    }

    public boolean beginPopupContextWindow(String a_strId, int a_mouseButton, boolean a_alsoOverItems) {
        return m_imGui.beginPopupContextWindow(a_strId, int2MB(a_mouseButton), a_alsoOverItems);
    }

    public void closeCurrentPopup() {
        m_imGui.closeCurrentPopup();
    }

    public void endPopup() {
        m_imGui.endPopup();
    }

    public void addCurve(Vec2 a_start, Vec2 a_control, Vec2 a_end, int a_color, int a_thickness) {

        Vec2 l1 = a_control.minus(a_start);
        Vec2 l2 = a_end.minus(a_control);

        ArrayList<Vec2> points = new ArrayList<>();

        final int segments = 32;
        for (int i = 0; i <= segments; i++) {
            float t = (float)i / (float)segments;

            Vec2 p1 = a_start.plus(l1.times(t));
            Vec2 p2 = a_control.plus(l2.times(t));

            Vec2 p = p1.plus(p2.minus(p1).times(t));

            points.add(p);

        }

        m_imGui.getWindowDrawList().addPolyline(points, a_color, false, a_thickness);

    }

    public void getCurvePoints(int a_segments, ArrayList<Vec2>a_outPoints, Vec2 a_start, Vec2 a_startControl, Vec2 a_end, Vec2 a_endControl) {
        Vec2 l1 = a_startControl.minus(a_start);
        Vec2 l2 = a_endControl.minus(a_startControl);
        Vec2 l3 = a_end.minus(a_endControl);



        final int segments = 32;
        for (int i = 0; i <= segments; i++) {
            float t = (float)i / (float)segments;

            Vec2 p1 = a_start.plus(l1.times(t));
            Vec2 p2 = a_startControl.plus(l2.times(t));
            Vec2 p3 = a_endControl.plus(l3.times(t));

            Vec2 p4 = p1.plus(p2.minus(p1).times(t));
            Vec2 p5 = p2.plus(p3.minus(p2).times(t));

            Vec2 p = p4.plus(p5.minus(p4).times(t));

            a_outPoints.add(p);
        }
    }

    public void addCurve(Vec2 a_start, Vec2 a_startControl, Vec2 a_end, Vec2 a_endControl, int a_color, int a_thickness) {

        ArrayList<Vec2> points = new ArrayList<>();
        getCurvePoints(32, points, a_start, a_startControl, a_end, a_endControl);

        m_imGui.getWindowDrawList().addPolyline(points, a_color, false, a_thickness);

    }

    public void addArrow(Vec2 m_start, Vec2 a_dir, int a_color) {
        Vec2 p1 = a_dir.plus(m_start.plus(a_dir.rotate((float)(Math.PI * 0.25))));
        Vec2 p2 = a_dir.plus(m_start.plus(a_dir.rotate((float)(Math.PI * -0.25))));

        m_imGui.getWindowDrawList().addTriangle(m_start, p2, p1, a_color, 1);
        /*addLine(m_start, p1, a_color, 1);
        addLine(m_start, p2, a_color, 1);*/

    }

    public boolean collapsingHeader(String a_text, int a_flags) {
        if (m_isWidgetDisabled) {
            m_imGui.popItemFlag();
        }
        boolean ret = m_imGui.collapsingHeader(a_text, a_flags);
        if (m_isWidgetDisabled) {
            m_imGui.pushItemFlag(ItemFlag.Disabled.i, true);
        }

        return ret;
    }


    protected static class DashContext {
        float m_remainingDrawLength;
        int m_ix;
    }

    public boolean menuItem(String a_label, String a_shortCut, boolean a_selected, boolean a_enabled) {
        return m_imGui.menuItem(a_label, a_shortCut, a_selected, a_enabled);
    }

    public void addDashedCircle(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_thickness, float a_holeLength, float a_dashlength) {
        DashContext dc = new DashContext();
        double segmentStep = 2 * Math.PI / a_segments;
        double angle = 0;

        for (int segment = 0; segment < a_segments; segment++) {
            float x1 = (float)Math.cos(angle) * a_radius;
            float y1 = (float)Math.sin(angle) * a_radius;
            angle += segmentStep;

            float x2 = (float)Math.cos(angle) * a_radius;
            float y2 = (float)Math.sin(angle) * a_radius;

            Vec2 p1 = new Vec2(x1, y1).plus(a_center);
            Vec2 p2 = new Vec2(x2, y2).plus(a_center);

            addDashedLine(p1, p2, a_color, a_thickness, a_holeLength, a_dashlength, dc);
        }

        //m_imGui.getWindowDrawList().addCircle(a_center, a_radius, a_color, a_segments, a_thickness);
    }

    public String getLongestSubString(String a_str, float a_maxLength, String a_regexSplitter) {
        String [] nameParts = a_str.split(a_regexSplitter);
        int nameIx = nameParts.length - 1;
        String name = nameParts[nameIx];
        nameIx --;

        while (nameIx >= 0) {

            String newName = nameParts[nameIx] + "." + name;
            Vec2 newTextSize = calcTextSize(newName, false);
            if (newTextSize.getX() > a_maxLength) {
                break;
            } else {
                name = newName;
            }

            nameIx--;
        }

        return name;
    }

    private Vec2 rotate(Vec2 a_pos, float a_angle) {

        // TODO: implement
        throw new NotImplementedException();
    }

    public Vec2 text(String a_text, Vec2 a_position, int a_color, float a_angle) {
        final int tId = m_imGui.getDefaultFont().containerAtlas.getTexID();

        float x = 0;
        float scale = m_imGui.getFont().getScale();
        float cosa = (float)Math.cos(a_angle);

        float height;
        {
            FontGlyph fg = m_imGui.getFont().findGlyph('l');
            height = (fg.getY0() - fg.getY1()) * scale;
        }



        if (cosa < 0) {
            a_angle -= Math.PI;

            for (int cIx = 0; cIx < a_text.length(); cIx++) {
                FontGlyph fg = m_imGui.getFont().findGlyph(a_text.charAt(cIx));

                x -= fg.getAdvanceX() * scale;
            }

        }

        Vec2 ret;

        Vec2 firstPos = new Vec2(), lastPos = new Vec2();


        {
            FontGlyph fg = m_imGui.getFont().findGlyph(a_text.charAt(0));
            Vec2 p2, p3;
            //float height = (fg.getY0() - fg.getY1()) * scale;

            // -3 to get some offset before the first character
            p2 = rotate(new Vec2((fg.getX1() - 5) * scale, (fg.getY1() * scale) + height*0.5), (a_angle));
            p3 = rotate(new Vec2((fg.getX1() - 5) * scale, (fg.getY0() * scale) + height*0.5), (a_angle));

            firstPos.setX(p2.getX() * 0.5f + p3.getX() * 0.5f);
            firstPos.setY(p2.getY() * 0.5f + p3.getY() * 0.5f);
        }



        for (int cIx = 0; cIx < a_text.length() - 1; cIx++) {
            FontGlyph fg = m_imGui.getFont().findGlyph(a_text.charAt(cIx));
            Vec2 p0, p1, p2, p3;
            //float height = (fg.getY0() - fg.getY1()) * scale;

            p0 = rotate(new Vec2((x + fg.getX0() * scale), (fg.getY0() * scale) + height*0.5), a_angle);
            p1 = rotate(new Vec2((x + fg.getX0() * scale), (fg.getY1() * scale) + height*0.5), a_angle);
            p2 = rotate(new Vec2((x + fg.getX1() * scale), (fg.getY1() * scale) + height*0.5), a_angle);
            p3 = rotate(new Vec2((x + fg.getX1() * scale), (fg.getY0() * scale) + height*0.5), a_angle);

            x = x + fg.getAdvanceX() * scale;

            m_imGui.getWindowDrawList().addImageQuad(tId, a_position.plus(p0), a_position.plus(p1), a_position.plus(p2), a_position.plus(p3),
                                                          new Vec2(fg.getU0(), fg.getV0()), new Vec2(fg.getU0(), fg.getV1()), new Vec2(fg.getU1(), fg.getV1()), new Vec2(fg.getU1(), fg.getV0()), a_color);
        }


        {
            FontGlyph fg = m_imGui.getFont().findGlyph(a_text.charAt(a_text.length() - 1));
            Vec2 p0, p1, p2, p3;
            //float height = (fg.getY0() - fg.getY1()) * scale;

            p0 = rotate(new Vec2((x + fg.getX0() * scale), (fg.getY0() * scale) + height*0.5), a_angle);
            p1 = rotate(new Vec2((x + fg.getX0() * scale), (fg.getY1() * scale) + height*0.5), a_angle);
            p2 = rotate(new Vec2((x + fg.getX1() * scale), (fg.getY1() * scale) + height*0.5), a_angle);
            p3 = rotate(new Vec2((x + fg.getX1() * scale), (fg.getY0() * scale) + height*0.5), a_angle);

            x = x + fg.getAdvanceX() * scale;

            lastPos.setX(p2.getX() * 0.5f + p3.getX() * 0.5f);
            lastPos.setY(p2.getY() * 0.5f + p3.getY() * 0.5f);

            m_imGui.getWindowDrawList().addImageQuad(tId, a_position.plus(p0), a_position.plus(p1), a_position.plus(p2), a_position.plus(p3),
                    new Vec2(fg.getU0(), fg.getV0()), new Vec2(fg.getU0(), fg.getV1()), new Vec2(fg.getU1(), fg.getV1()), new Vec2(fg.getU1(), fg.getV0()), a_color);
        }

        if (cosa < 0) {
            ret = lastPos;
        } else {
            ret = firstPos;
        }

        return a_position.plus(ret);
    }

    public void addFilledCircleSegment(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_startAngle, float a_endAngle) {
        double segmentStep = (a_endAngle - a_startAngle) / a_segments;
        double angle = a_startAngle;
        final double delta = 0.001;    // needed to avoid pixel leaks in borders between triangle segments


        // TODO: if this becomes a performance problem we could draw each half-circle as a convex polygon.
        for (int segment = 0; segment < a_segments; segment++) {
            float x1 = (float)Math.cos(angle) * a_radius;
            float y1 = (float)Math.sin(angle) * a_radius;
            angle += segmentStep;

            float x2 = (float)Math.cos(angle + delta) * a_radius;
            float y2 = (float)Math.sin(angle + delta) * a_radius;

            ArrayList<Vec2> points = new ArrayList<>();
            Vec2 p1 = new Vec2(x1, y1).plus(a_center);
            Vec2 p2 = new Vec2(x2, y2).plus(a_center);
            points.add(p1);
            points.add(p2);
            points.add(a_center);
            m_imGui.getWindowDrawList().addConvexPolyFilled(points, a_color);
        }
    }

    public void addCircleSegment(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_startAngle, float a_endAngle, float a_thickness) {
        double segmentStep = (a_endAngle - a_startAngle) / a_segments;
        double angle = a_startAngle;

        for (int segment = 0; segment < a_segments; segment++) {
            float x1 = (float)Math.cos(angle) * a_radius;
            float y1 = (float)Math.sin(angle) * a_radius;
            angle += segmentStep;

            float x2 = (float)Math.cos(angle) * a_radius;
            float y2 = (float)Math.sin(angle) * a_radius;

            Vec2 p1 = new Vec2(x1, y1).plus(a_center);
            Vec2 p2 = new Vec2(x2, y2).plus(a_center);

            addLine(p1, p2, a_color, a_thickness);
        }
    }

    public void addDashedCircleSegment(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_startAngle, float a_endAngle, float a_thickness, float a_holeLength, float a_dashlength) {
        addDashedCircleSegment(a_center, a_radius, a_color, a_segments, a_startAngle, a_endAngle, a_thickness, a_holeLength, a_dashlength, new DashContext());
    }

    public void addDashedCircleSegment(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_startAngle, float a_endAngle, float a_thickness, float a_holeLength, float a_dashlength, DashContext a_dc) {
        double segmentStep = (a_endAngle - a_startAngle) / a_segments;
        double angle = a_startAngle;


        for (int segment = 0; segment < a_segments; segment++) {
            float x1 = (float)Math.cos(angle) * a_radius;
            float y1 = (float)Math.sin(angle) * a_radius;
            angle += segmentStep;

            float x2 = (float)Math.cos(angle) * a_radius;
            float y2 = (float)Math.sin(angle) * a_radius;

            Vec2 p1 = new Vec2(x1, y1).plus(a_center);
            Vec2 p2 = new Vec2(x2, y2).plus(a_center);

            addDashedLine(p1, p2, a_color, a_thickness, a_holeLength, a_dashlength, a_dc);
        }
    }

    public void addDashedRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_thickness, float a_holeLength, float a_dashLength, float a_rounding) {
        Vec2 bl = new Vec2(a_tl.getX(), a_br.getY());
        Vec2 tr = new Vec2(a_br.getX(), a_tl.getY());
        Vec2 dX = new Vec2(a_rounding, 0);
        Vec2 dY = new Vec2(0, a_rounding);

        final int segments = (int)(a_rounding / 2);

        DashContext dc = new DashContext();

        addDashedLine(a_tl.plus(dX), tr.minus(dX), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(tr.minus(dX).plus(dY), a_rounding, a_color, segments, (float)Math.PI / 2 * 3, (float)Math.PI * 2, a_thickness, a_holeLength, a_dashLength, dc);

        addDashedLine(tr.plus(dY), a_br.minus(dY), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(a_br.minus(dX).minus(dY), a_rounding, a_color, segments, 0, (float)Math.PI / 2 * 1, a_thickness, a_holeLength, a_dashLength, dc);

        addDashedLine(a_br.minus(dX), bl.plus(dX), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(bl.plus(dX).minus(dY), a_rounding, a_color, segments, (float)Math.PI / 2 * 1, (float)Math.PI / 2 * 2, a_thickness, a_holeLength, a_dashLength, dc);

        addDashedLine(bl.minus(dY), a_tl.plus(dY), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(a_tl.plus(dX).plus(dY), a_rounding, a_color, segments, (float)Math.PI / 2 * 2, (float)Math.PI / 2 * 3, a_thickness, a_holeLength, a_dashLength, dc);


        //m_imGui.getWindowDrawList().addRect(a_tl, a_br, a_color, a_rounding, a_corners, a_thickness);
    }

    public void addDashedRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_thickness, float a_holeLength, float a_dashLength) {
        Vec2 bl = new Vec2(a_tl.getX(), a_br.getY());
        Vec2 tr = new Vec2(a_br.getX(), a_tl.getY());

        DashContext dc = new DashContext();

        addDashedLine(a_tl, tr, a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedLine(tr, a_br, a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedLine(a_br, bl, a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedLine(bl, a_tl, a_color, a_thickness, a_holeLength, a_dashLength, dc);


        //m_imGui.getWindowDrawList().addRect(a_tl, a_br, a_color, a_rounding, a_corners, a_thickness);
    }

    public void addDashedLine(Vec2 a_p1, Vec2 a_p2, int a_color, float a_thickness, float a_holeLength, float a_dashlength, DashContext a_dc) {
        Vec2 dir = a_p2.minus(a_p1);
        float lineLength = dir.length();
        dir.div(lineLength, dir);

        float drawnLength = 0;

        // draw the remaining part of the previous command
        if (a_dc.m_remainingDrawLength > 0) {
            if(a_dc.m_ix % 2 == 0) {
                Vec2 p1 = a_p1.plus(dir.times(drawnLength));
                drawnLength += a_dc.m_remainingDrawLength;
                if (drawnLength > lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    m_imGui.getWindowDrawList().addLine(p1, a_p2, a_color, a_thickness);
                    return;
                } else {
                    Vec2 p2 = a_p1.plus(dir.times(drawnLength));
                    m_imGui.getWindowDrawList().addLine(p1, p2, a_color, a_thickness);
                    a_dc.m_ix++;

                }
            } else {
                drawnLength += a_dc.m_remainingDrawLength;
                if (drawnLength > lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    return;
                } else {
                    a_dc.m_ix++;
                }
            }
        }

        while(true) {
            if (a_dc.m_ix % 2 == 0) {
                Vec2 p1 = a_p1.plus(dir.times(drawnLength));
                drawnLength += a_dashlength;
                if (drawnLength > lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    m_imGui.getWindowDrawList().addLine(p1, a_p2, a_color, a_thickness);
                    return;
                } else {
                    Vec2 p2 = a_p1.plus(dir.times(drawnLength));
                    m_imGui.getWindowDrawList().addLine(p1, p2, a_color, a_thickness);
                }
            } else {
                drawnLength += a_holeLength;
                if (drawnLength > lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    return;
                }
            }
            a_dc.m_ix++;
        }
    }

    public ImGuiWrapper(ImGui a_imgui) {
        m_imGui = a_imgui;
    }

    public enum InputTextStatus {
        Editing,
        Done,
        Canceled
    }

    public String inputTextSingleLine(String a_label, String a_text) {


        // we may copy a really large chunk of data from the clipboard so we need to handle that
        //String clipboard = imgui().getIo().getGetClipboardTextFn() != null ? imgui().getIo().getGetClipboardTextFn().invoke(imgui().getIo().getClipboardUserData()).toString() : "";
        // TODO: This is not a good way to handle this but I can not seem to get a hold of the clipboard data or use any callbacks.
        try {
            byte[] buffer = new byte[a_text.length() + g_textBufferExtraSize*2];
            byte[] textBytes = a_text.getBytes();
            Arrays.fill(buffer, (byte)'\0');
            for (int i = 0; i < textBytes.length && i < buffer.length; i++) {
                buffer[i] = textBytes[i];
            }

            if (m_imGui.inputText(a_label, buffer, 0, null, null)) {    // TODO: this api call has changed, possibly make this better
                int i; for (i = 0; i < buffer.length && buffer[i] != 0; i++) { }
                String txt = new String(buffer, 0, i, StandardCharsets.UTF_8);
                return txt;
            }
            return a_text;
        } catch (ArrayIndexOutOfBoundsException e) {
            g_textBufferExtraSize *= 2;
            imgui().clearActiveID();
            return a_text;
            //rounds++;
        }
    }

    public InputTextStatus inputTextSingleLine(Vec2 a_pos, float a_width, String a_label, char[] a_buffer) {
        Vec2 textPos = a_pos.minus(m_imGui.getWindowPos());

        textPos.minus(m_imGui.getStyle().getFramePadding(), textPos);

        m_imGui.setCursorPos(textPos);
        m_imGui.pushItemWidth(a_width + m_imGui.getStyle().getFramePadding().getX() * 1 + m_imGui.getFontSize());   // we need to be a bit wider than *2 so that imgui does not make wonky things with the text x position

        byte[] buffer = new String(a_buffer).getBytes();
        boolean ret = m_imGui.inputText(a_label, buffer, InputTextFlag.EnterReturnsTrue.i, null, null);   // TODO: this api call has changed

        int i; for (i = 0; i < buffer.length && buffer[i] != 0; i++) { }
        String txt = new String(buffer, 0, i, StandardCharsets.UTF_8);
        for (i = 0; i < a_buffer.length && i < txt.length(); i++) {
            a_buffer[i] = txt.charAt(i);
        }

        // GLFW_KEY_KP_ENTER   335
        if (m_imGui.isKeyDown(335)) {
            ret = true;
        }
        m_imGui.popItemWidth();
        if (ret) {

            return InputTextStatus.Done;
        }

        if (m_imGui.isAnyItemActive()) {
            return InputTextStatus.Editing;
        }

        return InputTextStatus.Canceled;
    }

    public void addRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners, float a_thickness) {

        m_imGui.getWindowDrawList().addRect(a_tl, a_br, a_color, a_rounding, a_corners, a_thickness);
    }
    public void addRectFilled(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners) {
        m_imGui.getWindowDrawList().addRectFilled(a_tl, a_br, a_color, a_rounding, a_corners);
    }

    public void addText(Vec2 a_pos, int a_color, String a_text) {
        m_imGui.getWindowDrawList().addText(a_pos, a_color, a_text);
    }

    public void addCircle(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_thickness) {
        m_imGui.getWindowDrawList().addCircle(a_center, a_radius, a_color, a_segments, a_thickness);
    }

    public void addCircleFilled(Vec2 a_center, float a_radius, int a_color, int a_segments) {
        m_imGui.getWindowDrawList().addCircleFilled(a_center, a_radius, a_color, a_segments);
    }

    public void addLine(Vec2 a_p1, Vec2 a_p2, int a_color, float a_thickness) {
        m_imGui.getWindowDrawList().addLine(a_p1, a_p2, a_color, a_thickness);
    }

    public float getTextLineHeightWithSpacing() {
        return m_imGui.getTextLineHeightWithSpacing();
    }

    public Vec2 calcTextSize(String a_str, boolean a_hideTextAfterDoubleHash) {
        return m_imGui.calcTextSize(a_str, a_hideTextAfterDoubleHash, -1);
    }

    public boolean beginTooltip() {

        // as it seems a too large tool tip can crash the application
        // we ned to restore the clipRect stack to prevent further problems
        Stack<Vec4> clipRectStack = new Stack<>();
        clipRectStack.addAll(m_imGui.getCurrentWindow().getDrawList().get_clipRectStack());
        try {
            m_imGui.beginTooltip();
            return true;
        } catch (Exception e) {

            try {
                m_imGui.endTooltip();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();

            // restore the clip rect stack
            Stack<Vec4> s = m_imGui.getCurrentWindow().getDrawList().get_clipRectStack();
            s.clear();
            s.addAll(clipRectStack);

            return false;
        }
    }

    public void pushDisableWidgets() {
        if (!m_isWidgetDisabled) {
            m_isWidgetDisabled = true;
            m_imGui.pushItemFlag(ItemFlag.Disabled.i, true);
            m_imGui.pushStyleVar(StyleVar.Alpha, m_imGui.getStyle().getAlpha() * 0.5f);
        }
    }

    public void popDisableWidgets() {
        if (m_isWidgetDisabled) {
            m_imGui.popItemFlag();
            m_imGui.popStyleVar(1);
            m_isWidgetDisabled = false;
        }
    }

    public void endTooltip() {
        m_imGui.endTooltip();
    }

    public void text(String a_text) {
        m_imGui.text(a_text);
    }

    public Vec2 getMousePos() {
        return m_imGui.getMousePos();
    }

    public boolean isMouseDoubleClicked(int a_button) {
        return m_imGui.isMouseDoubleClicked(int2MB(a_button));
    }

    public boolean isMouseClicked(int a_button, boolean a_doRepeat) {
        return m_imGui.isMouseClicked(int2MB(a_button), a_doRepeat);
    }

    // TODO: Now that the api has changed we should also have a mouse button enum in our wrapper
    public MouseButton int2MB(int a_button) {
        imgui.MouseButton mb = imgui.MouseButton.Left;
        for (int i = 0; i < MouseButton.values().length; i++) {
            if (a_button == MouseButton.values()[i].getI()) {
                mb = MouseButton.values()[i];
                break;
            }
        }
        return mb;
    }

    public void sameLine(int a_offsetFromStartX) {
        m_imGui.sameLine(a_offsetFromStartX, -1);
    }

    boolean isMouseDown(int a_button) {
        return m_imGui.isMouseDown(int2MB(a_button));
    }

    public Vec2 getMouseDragDelta(int a_button, float a_lockThreshold) {
        return m_imGui.getMouseDragDelta(int2MB(a_button), 1.0f);
    }

    public boolean isMouseDragging(int a_button, float a_lockThreshold) {
        return m_imGui.isMouseDragging(int2MB(a_button), a_lockThreshold);
    }

    public void stopWindowDrag() {
        Window wnd = m_imGui.getCurrentWindow();

        while (wnd.getParentWindow() != null) {
            wnd = wnd.getParentWindow();
        }
        wnd.setFlags(WindowFlag.NoMove.or(wnd.getFlags()));
    }

    public boolean isInside(Vec2 a_center, float a_radius, Vec2 a_pos) {
        if (isCurrentWindowActive()) {
            return a_center.minus(a_pos).length2() <= a_radius * a_radius;
        }
        return false;
    }

    public static Vec4 fromColor(int a_color) {
        Vec4 col = new Vec4();
        final int R_SHIFT = 0;
        final int G_SHIFT = 8;
        final int B_SHIFT = 16;
        final int A_SHIFT = 24;

        col.setX((float)((a_color >> R_SHIFT) & 0xFF) / 255.0f);
        col.setY((float)((a_color >> G_SHIFT) & 0xFF) / 255.0f);
        col.setZ((float)((a_color >> B_SHIFT) & 0xFF) / 255.0f);
        col.setW((float)((a_color >> A_SHIFT) & 0xFF) / 255.0f);

        return col;
    }

    public static int toColor(int a_r, int a_g, int a_b, int a_a) {
        return COL32(a_r, a_g, a_b, a_a);
    }

    public static int toColor(Vec4 a_v) {
        return COL32((int)(a_v.getX() * 255), (int)(a_v.getY() * 255), (int)(a_v.getZ() * 255), (int)(a_v.getW() * 255));
    }

    public boolean isCurrentWindowInFocus() {
        return m_imGui.isWindowFocused(FocusedFlag.RootAndChildWindows);
    }

    public boolean isCurrentWindowActive() {
        return m_imGui.getCurrentWindow().isActiveAndVisible() && m_imGui.isWindowHovered(HoveredFlag.RootAndChildWindows);
    }



    public boolean isInside(Vec2 a_p1, Vec2 a_p2, double a_thickness, Vec2 a_pos) {
        if (isCurrentWindowActive()) {
            Vec2 p2p1 = a_p2.minus(a_p1);
            float l2 = p2p1.length2();
            // Consider the line extending the segment, parameterized as v + t (w - v).
            // We find projection of point p onto the line.
            // It falls where t = [(p-v) . (w-v)] / |w-v|^2
            // We clamp t from [0,1] to handle points outside the segment vw.
            float t = Math.max(0, Math.min(1, a_pos.minus(a_p1).dot(p2p1) / l2));
            Vec2 projection = a_p1.plus(p2p1.times(t));  // Projection falls on the segment
            return projection.minus(a_pos).length2() < a_thickness * a_thickness;

        }
        return false;
    }

    public boolean isInside(Rect a_rect, Vec2 a_pos) {
        if (isCurrentWindowActive()) {
            /*Vec4 clipRect = m_imGui.getCurrentWindow().getDrawList().getCurrentClipRect();
            if (clipRect != null) {
                return a_rect.contains(a_pos) && new Rect(clipRect).contains(a_pos);
            }*/
            return a_rect.contains(a_pos);
        }

        return false;
    }

    public boolean isInsideClipRect(Vec2 a_pos) {
        Vec4 clipRect = m_imGui.getCurrentWindow().getDrawList().getCurrentClipRect();
        if (clipRect != null) {
            return new Rect(clipRect).contains(a_pos);
        }
        return true;
    }

    public boolean button(String a_text, float a_width) {
        return m_imGui.button(a_text, new Vec2(a_width, 0));
    }
}
