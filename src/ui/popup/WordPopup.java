/* 
 * Copyright (C) 2017 Laurens Weyn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ui.popup;

import hooker.ClipboardHook;
import language.splitter.FoundWord;
import ui.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Laurens Weyn
 */
public class WordPopup extends JPopupMenu
{
    FoundWord word;
    
    JMenuItem addBreak;
    JMenuItem exportLine;
    JMenuItem copy;
    JMenuItem append;
    JMenuItem copyFull;
    JCheckBoxMenuItem markKnown;
    int x, y;
    UI ui;
    public WordPopup(FoundWord word, UI ui)
    {
        this.word = word;
        this.ui = ui;
        String clipboard = ClipboardHook.getClipboard();
        exportLine = new JMenuItem(new AbstractAction("Export whole line")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);//ensure this menu's already gone for the screenshot
                exportLine();
            }
        });
        copy = new JMenuItem(new AbstractAction("Copy word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(word.getText());
            }
        });
        append = new JMenuItem(new AbstractAction("Append word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(clipboard + word.getText());
            }
        });
        copyFull = new JMenuItem(new AbstractAction("Copy whole line")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(UI.text);
            }
        });
        addBreak = new JMenuItem(new AbstractAction("Toggle break here (middle click)")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("adding break");
                //simulate middle button press at window origin (placing a break there)
                ui.mouseClicked(new MouseEvent(ui.disp.getFrame(), -1, System.currentTimeMillis(), 0, x, y, 1, true, 2));
            }
        });
        
        markKnown = new JCheckBoxMenuItem(new AbstractAction("I know this word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(markKnown.isSelected())
                {
                    UI.known.setKnown(word);
                }
                else
                {
                    UI.known.setUnknown(word);
                }
                ui.render();//change color of word
            }
        });
        markKnown.setSelected(UI.known.isKnown(word));
        
        add(addBreak);
        add(exportLine);
        add(markKnown);
        add(new Separator());
        add(copy);
        if(UI.text.contains(clipboard) && UI.text.contains(clipboard + word.getText()))add(append);
        add(copyFull);

        addPopupMenuListener(new IgnoreExitListener());
    }
    public void show(int x, int y)
    {
        this.x = x;
        this.y = y;
        show(ui.disp.getFrame(), x, y);
    }
    public static void exportLine()
    {
        JFrame frame = UI.instance.disp.getFrame();
        try
        {
            Date date = new Date();
            DateFormat df = new SimpleDateFormat(UI.options.getOption("timeStampFormat"));
            File textFile = new File(UI.options.getOption("lineExportPath"));
            String note = "";

            if(UI.options.getOptionBool("commentOnExportLine"))
            {
                note = (String)JOptionPane.showInputDialog(frame,
                                                           "Enter comment\n(You may also leave this blank)",
                                                           "Exporting line",
                                                           JOptionPane.PLAIN_MESSAGE,
                                                           null,
                                                           null,
                                                           UI.userComment);
                if(note == null)return;//cancelled

                UI.userComment = note;//update for next time
                Thread.sleep(500);//give the popup time to disappear
            }

            Writer fr = new OutputStreamWriter(new FileOutputStream(textFile, true), Charset.forName("UTF-8"));
            fr.append(df.format(date) + "\t" + UI.text.replace("\n", "<br>") + "\t" + note + "\n");
            fr.close();
            //take a screenshot with the exported line
            if(UI.options.getOptionBool("exportImage"))
            {
                File imageFolder = new File(UI.options.getOption("screenshotExportPath"));
                if (!imageFolder.exists())
                {
                    boolean success = imageFolder.mkdirs();
                    if (!success) throw new IOException("Failed to create folder(s) for screenshots: directory"
                                                                + UI.options.getOption("screenshotExportPath"));
                }
                Robot robot = new Robot();
                Point pos = frame.getLocationOnScreen();
                Rectangle area;
                if(UI.options.getOptionBool("fullscreenScreenshot"))
                {
                    //whole screen
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    area = new Rectangle(0, 0, screenSize.width, screenSize.height);
                }
                else
                {
                    //game area
                    if(UI.options.getOptionBool("defsShowUpwards"))
                    {
                        area = new Rectangle(pos.x, pos.y + UI.furiganaStartY - UI.options.getOptionInt("maxHeight"),
                                UI.options.getOptionInt("windowWidth"),
                                UI.options.getOptionInt("maxHeight"));
                    }
                    else
                    {
                        area = new Rectangle(pos.x, pos.y + UI.defStartY,
                                UI.options.getOptionInt("windowWidth"),
                                UI.options.getOptionInt("maxHeight"));
                    }
                }

                //hide Spark Reader and take the screenshot
                UI.hidden = true;
                UI.instance.render();
                BufferedImage screenshot = robot.createScreenCapture(area);
                UI.hidden = false;
                UI.instance.render();

                String fileName = imageFolder.getAbsolutePath();
                if(!fileName.endsWith("/") && !fileName.endsWith("\\"))fileName += "/";
                fileName += df.format(date) + ".png";

                System.out.println("saving screenshot as " + fileName);
                ImageIO.write(screenshot, "png", new File(fileName));
            }

        }catch(IOException | AWTException | InterruptedException err)
        {
            JOptionPane.showInputDialog(frame, "Error while exporting line:\n" + err);
        }
    }

}