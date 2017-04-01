package options.page;

import language.dictionary.DefSource;
import language.dictionary.UserDefinition;
import main.Main;
import ui.WordEditUI;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * OptionPage to allow editing user definitions
 * Created by Laurens on 2/25/2017.
 */
public class UserDefPage implements Page
{

    private JPanel mainPanel, buttonPanel;
    private JList<UserDefinition> definitionList;
    private DefListModel listModel;
    private JScrollPane defScroll;
    private DefSource source;
    public UserDefPage(DefSource source)
    {
        this.source = source;
        listModel = new DefListModel(source);
        definitionList = new JList<>(listModel);
        defScroll = new JScrollPane(definitionList);
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton(new AbstractAction("Add")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new WordEditUI();
            }
        });
        JButton editButton = new JButton(new AbstractAction("Edit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new WordEditUI(definitionList.getSelectedValue());
            }
        });
        JButton deleteButton = new JButton(new AbstractAction("Delete")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for(UserDefinition definition:definitionList.getSelectedValuesList())
                {
                    Main.dict.removeDefinition(definition);
                    source.getDefinitions().remove(definition);
                    //TODO save these changes
                    listModel.update();
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);



        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(defScroll, BorderLayout.CENTER);
        //mainPanel.add(buttonPanel, BorderLayout.NORTH);
    }
    @Override
    public JComponent getComponent()
    {
        return mainPanel;
    }

    @Override
    public void update()
    {
        //TODO unload and reload user dictionary
    }

    private class DefListModel implements ListModel<UserDefinition>
    {
        DefSource source;
        DefListModel(DefSource source)
        {
            this.source = source;
        }
        @Override
        public int getSize()
        {
            return source.getDefinitions().size();
        }

        @Override
        public UserDefinition getElementAt(int index)
        {
            return (UserDefinition)source.getDefinitions().get(index);
        }
        ArrayList<ListDataListener> listeners = new ArrayList<>();
        @Override
        public void addListDataListener(ListDataListener l)
        {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l)
        {
            listeners.remove(l);
        }

        public void update()
        {
            //TODO don't be lazy on listDataEvent here
            ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
            for(ListDataListener listener:listeners)
            {
                listener.contentsChanged(event);
            }
        }
    }

    @Override
    public String toString()
    {
        return /*"edit " + */source.getName().toLowerCase() + " dictionary";
    }
}
