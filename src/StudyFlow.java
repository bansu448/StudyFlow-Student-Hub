
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Base64;

public class StudyFlow extends JFrame {
    static final Color BG=new Color(12,17,32), PANEL=new Color(25,34,57), PANEL2=new Color(34,45,73);
    static final Color WHITE=new Color(245,247,255), MUTED=new Color(177,190,220), PURPLE=new Color(124,92,255);
    static final Color CYAN=new Color(36,211,238), GREEN=new Color(52,211,153), RED=new Color(244,93,122);
    static final Font NORMAL=new Font("Segoe UI",Font.PLAIN,14), BOLD=new Font("Segoe UI",Font.BOLD,14);
    static final Path SAVE=Paths.get(System.getProperty("user.home"),".studyflow","tasks.txt");

    CardLayout navLayout=new CardLayout();
    JPanel pages=new JPanel(navLayout);
    List<Task> tasks=new ArrayList<>();
    DefaultListModel<Task> visibleModel=new DefaultListModel<>();
    JList<Task> taskList;
    JTextField taskTitle,subject,due,search;
    JTextArea notes;
    JComboBox<String> priority,filter;
    JLabel totalValue,doneValue,progressValue;
    int xp=0, qIndex=0, score=0, seconds=30;
    String mode="Code Arena";
    javax.swing.Timer clock;
    JLabel qText,feedback,timeText,scoreText;
    JPanel options;
    JButton next;
    List<Question> questions=new ArrayList<>();
    boolean answered=false;

    static class Task {
        String title,subject,due,priority,notes; boolean done;
        Task(String a,String b,String c,String d,String e,boolean f){title=a;subject=b;due=c;priority=d;notes=e;done=f;}
        public String toString(){return (done?"✓  ":"○  ")+title+"   ·   "+subject+"   ·   "+due+"   ·   "+priority;}
    }
    static class Question {
        String topic,prompt,explain;String[] choices;int answer;
        Question(String t,String p,String[] c,int a,String e){topic=t;prompt=p;choices=c;answer=a;explain=e;}
    }

    public StudyFlow(){
        super("StudyFlow | Student Hub");
        setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1180,780);setMinimumSize(new Dimension(960,650));setLocationRelativeTo(null);
        getContentPane().setBackground(BG);pages.setBackground(BG);
        pages.add(dashboard(),"home");pages.add(planner(),"planner");pages.add(gameMenu(),"games");pages.add(gamePage(),"game");pages.add(chat(),"chat");
        add(pages);load();refreshList();refreshStats();navLayout.show(pages,"home");
    }
    JLabel label(String s,int size,Color color,boolean bold){JLabel l=new JLabel(s);l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));l.setForeground(color);return l;}
    JPanel card(){JPanel p=new JPanel();p.setBackground(PANEL);p.setBorder(new CompoundBorder(new LineBorder(new Color(58,72,108),1,true),new EmptyBorder(18,18,18,18)));return p;}
    JButton button(String text,Color color){
        JButton b=new JButton(text);
        b.setUI(new BasicButtonUI()); b.setFont(new Font("Segoe UI",Font.BOLD,14));
        b.setForeground(Color.WHITE);b.setBackground(color);b.setOpaque(true);
        b.setContentAreaFilled(true);b.setBorderPainted(false);b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12,18,12,18));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){if(b.isEnabled())b.setBackground(color.brighter());}public void mouseExited(MouseEvent e){b.setBackground(color);}});
        return b;
    }
    void styleInput(JComponent c){c.setFont(NORMAL);c.setForeground(WHITE);c.setBackground(PANEL2);c.setBorder(new CompoundBorder(new LineBorder(new Color(76,91,130),1,true),new EmptyBorder(8,10,8,10)));if(c instanceof JTextField)((JTextField)c).setCaretColor(CYAN);}
    JPanel shell(String title,String subtitle){
        JPanel root=new JPanel(new BorderLayout(18,18));root.setBackground(BG);root.setBorder(new EmptyBorder(22,26,24,26));
        JPanel head=new JPanel(new BorderLayout(12,0));head.setOpaque(false);
        JPanel titles=new JPanel();titles.setOpaque(false);titles.setLayout(new BoxLayout(titles,BoxLayout.Y_AXIS));
        titles.add(label(title,25,WHITE,true));titles.add(Box.createVerticalStrut(5));titles.add(label(subtitle,13,MUTED,false));head.add(titles,BorderLayout.WEST);
        JPanel nav=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));nav.setOpaque(false);
        nav.add(nav("Dashboard","home"));nav.add(nav("Planner","planner"));nav.add(nav("Game Zone","games"));nav.add(nav("Study Buddy","chat"));
        head.add(nav,BorderLayout.EAST);root.add(head,BorderLayout.NORTH);return root;
    }
    JButton nav(String text,String page){JButton b=button(text,new Color(51,65,98));b.setFont(new Font("Segoe UI",Font.BOLD,12));b.setBorder(new EmptyBorder(10,14,10,14));b.addActionListener(e->{if(page.equals("home"))refreshStats();navLayout.show(pages,page);});return b;}

    JPanel dashboard(){
        JPanel p=shell("STUDYFLOW  /  STUDENT HUB","PLAN SMARTER  •  PRACTICE DAILY  •  LEVEL UP YOUR TECH SKILLS");
        JPanel body=new JPanel();body.setOpaque(false);body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        JPanel hero=card();hero.setLayout(new BorderLayout(20,10));
        JPanel ht=new JPanel();ht.setOpaque(false);ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        ht.add(label("Your semester. Your mission.",30,WHITE,true));ht.add(Box.createVerticalStrut(10));
        ht.add(label("Organise assignments, practise coding and build real tech confidence.",15,MUTED,false));
        ht.add(Box.createVerticalStrut(12));ht.add(label("ONE PLACE FOR YOUR STUDY LIFE",11,CYAN,true));hero.add(ht,BorderLayout.CENTER);
        JButton add=button("+  ADD A STUDY TASK",PURPLE);add.addActionListener(e->navLayout.show(pages,"planner"));hero.add(add,BorderLayout.EAST);
        body.add(hero);body.add(Box.createVerticalStrut(16));
        JPanel stats=new JPanel(new GridLayout(1,3,14,0));stats.setOpaque(false);
        stats.add(statCard("TOTAL TASKS","0","Everything on your list",0));stats.add(statCard("COMPLETED","0","Celebrate every small win",1));stats.add(statCard("STUDY PROGRESS","0%","Keep moving forward",2));
        body.add(stats);body.add(Box.createVerticalStrut(16));
        JPanel features=new JPanel(new GridLayout(1,2,14,0));features.setOpaque(false);
        features.add(feature("01  TECHQUEST GAME ZONE","JAVA  •  PYTHON  •  DEBUGGING  •  MACHINE LEARNING","Solve short challenges, earn XP and improve your developer skills.","EXPLORE CHALLENGES",CYAN,"games"));
        features.add(feature("02  STUDY BUDDY","YOUR QUICK STUDY COMPANION","Get simple explanations for programming, ML concepts and study questions.","ASK STUDY BUDDY",new Color(255,190,85),"chat"));
        body.add(features);body.add(Box.createVerticalGlue());
        p.add(body,BorderLayout.CENTER);return p;
    }
    JPanel statCard(String title,String value,String desc,int index){
        JPanel c=card();c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.add(label(title,11,MUTED,true));c.add(Box.createVerticalStrut(8));
        JLabel v=label(value,30,WHITE,true);if(index==0)totalValue=v;if(index==1)doneValue=v;if(index==2)progressValue=v;
        c.add(v);c.add(Box.createVerticalStrut(5));c.add(label(desc,12,MUTED,false));return c;
    }
    JPanel feature(String title,String tag,String desc,String action,Color accent,String page){
        JPanel c=card();c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.add(label(title,19,accent,true));c.add(Box.createVerticalStrut(12));
        c.add(label(tag,11,WHITE,true));c.add(Box.createVerticalStrut(8));JTextArea d=new JTextArea(desc);d.setWrapStyleWord(true);d.setLineWrap(true);d.setEditable(false);d.setOpaque(false);d.setForeground(MUTED);d.setFont(NORMAL);d.setRows(2);c.add(d);c.add(Box.createVerticalGlue());
        JButton b=button(action+"  →",accent.equals(CYAN)?new Color(26,130,151):new Color(160,105,31));b.setAlignmentX(LEFT_ALIGNMENT);b.addActionListener(e->navLayout.show(pages,page));c.add(b);return c;
    }

    JPanel planner(){
        JPanel p=shell("MY STUDY PLANNER","Add tasks, manage deadlines and track your progress");
        JPanel body=new JPanel(new BorderLayout(14,14));body.setOpaque(false);
        JPanel form=card();form.setLayout(new GridBagLayout());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(5,4,5,4);g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;g.gridx=0;g.gridwidth=2;
        taskTitle=new JTextField();subject=new JTextField();due=new JTextField(LocalDate.now().toString());priority=new JComboBox<>(new String[]{"High","Medium","Low"});notes=new JTextArea(3,15);notes.setLineWrap(true);
        styleInput(taskTitle);styleInput(subject);styleInput(due);styleInput(priority);styleInput(notes);
        field(form,g,0,"TASK NAME",taskTitle);field(form,g,2,"SUBJECT / TOPIC",subject);field(form,g,4,"DUE DATE  (YYYY-MM-DD)",due);field(form,g,6,"PRIORITY",priority);
        g.gridy=8;form.add(label("NOTES",11,MUTED,true),g);g.gridy=9;form.add(new JScrollPane(notes),g);
        JPanel actions=new JPanel(new GridLayout(2,2,8,8));actions.setOpaque(false);
        JButton add=button("+ ADD TASK",PURPLE),edit=button("UPDATE",new Color(46,117,159)),del=button("DELETE",new Color(167,65,91)),complete=button("TOGGLE DONE",new Color(34,132,94));
        add.addActionListener(e->addTask());edit.addActionListener(e->editTask());del.addActionListener(e->deleteTask());complete.addActionListener(e->toggleTask());
        actions.add(add);actions.add(edit);actions.add(del);actions.add(complete);g.gridy=10;form.add(actions,g);
        JPanel list=card();list.setLayout(new BorderLayout(8,10));
        JPanel filters=new JPanel(new BorderLayout(8,0));filters.setOpaque(false);search=new JTextField();styleInput(search);search.setToolTipText("Search by task, subject or notes");
        filter=new JComboBox<>(new String[]{"All tasks","Pending","Completed","High priority"});styleInput(filter);filters.add(search,BorderLayout.CENTER);filters.add(filter,BorderLayout.EAST);
        taskList=new JList<>(visibleModel);taskList.setFont(new Font("Segoe UI",Font.PLAIN,14));taskList.setForeground(WHITE);taskList.setBackground(PANEL2);taskList.setSelectionBackground(PURPLE);taskList.setSelectionForeground(Color.WHITE);taskList.setFixedCellHeight(42);
        taskList.setBorder(new EmptyBorder(6,8,6,8));taskList.addListSelectionListener(e->{if(!e.getValueIsAdjusting())fillFormFromSelection();});
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){refreshList();}public void removeUpdate(javax.swing.event.DocumentEvent e){refreshList();}public void changedUpdate(javax.swing.event.DocumentEvent e){refreshList();}});
        filter.addActionListener(e->refreshList());
        list.add(filters,BorderLayout.NORTH);list.add(new JScrollPane(taskList),BorderLayout.CENTER);
        body.add(form,BorderLayout.WEST);body.add(list,BorderLayout.CENTER);p.add(body,BorderLayout.CENTER);return p;
    }
    void field(JPanel p,GridBagConstraints g,int y,String title,JComponent input){g.gridy=y;p.add(label(title,11,MUTED,true),g);g.gridy=y+1;p.add(input,g);}
    void addTask(){
        String t=taskTitle.getText().trim(),s=subject.getText().trim(),d=due.getText().trim();
        if(t.isEmpty()||s.isEmpty()){message("Enter a task name and subject.");return;}
        try{LocalDate.parse(d);}catch(Exception e){message("Date must use YYYY-MM-DD, for example 2026-11-25.");return;}
        tasks.add(new Task(t,s,d,(String)priority.getSelectedItem(),notes.getText().trim(),false));save();refreshList();refreshStats();clearForm();message("Task added successfully!");
    }
    void editTask(){Task old=taskList.getSelectedValue();if(old==null){message("Select a task from the list first.");return;}
        String t=taskTitle.getText().trim(),s=subject.getText().trim(),d=due.getText().trim();if(t.isEmpty()||s.isEmpty()){message("Enter a task name and subject.");return;}
        try{LocalDate.parse(d);}catch(Exception e){message("Date must use YYYY-MM-DD.");return;}
        old.title=t;old.subject=s;old.due=d;old.priority=(String)priority.getSelectedItem();old.notes=notes.getText().trim();save();refreshList();refreshStats();message("Task updated.");
    }
    void deleteTask(){Task t=taskList.getSelectedValue();if(t==null){message("Select a task to delete.");return;}int ok=JOptionPane.showConfirmDialog(this,"Delete '"+t.title+"'?","Confirm delete",JOptionPane.YES_NO_OPTION);if(ok==JOptionPane.YES_OPTION){tasks.remove(t);save();refreshList();refreshStats();clearForm();}}
    void toggleTask(){Task t=taskList.getSelectedValue();if(t==null){message("Select a task first.");return;}t.done=!t.done;save();refreshList();refreshStats();}
    void fillFormFromSelection(){Task t=taskList.getSelectedValue();if(t==null)return;taskTitle.setText(t.title);subject.setText(t.subject);due.setText(t.due);priority.setSelectedItem(t.priority);notes.setText(t.notes);}
    void clearForm(){taskTitle.setText("");subject.setText("");due.setText(LocalDate.now().toString());notes.setText("");taskList.clearSelection();}
    void refreshList(){if(visibleModel==null)return;visibleModel.clear();String q=search==null?"":search.getText().trim().toLowerCase();String f=filter==null?"All tasks":(String)filter.getSelectedItem();
        for(Task t:tasks){boolean ok=(t.title+" "+t.subject+" "+t.notes).toLowerCase().contains(q);if(f.equals("Pending")&&t.done)ok=false;if(f.equals("Completed")&&!t.done)ok=false;if(f.equals("High priority")&&!t.priority.equals("High"))ok=false;if(ok)visibleModel.addElement(t);}
    }
    void refreshStats(){if(totalValue==null)return;int done=0;for(Task t:tasks)if(t.done)done++;totalValue.setText(""+tasks.size());doneValue.setText(""+done);progressValue.setText((tasks.isEmpty()?0:done*100/tasks.size())+"%");}
    String enc(String s){return Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    String dec(String s){return new String(Base64.getDecoder().decode(s),java.nio.charset.StandardCharsets.UTF_8);}
    void save(){try{Files.createDirectories(SAVE.getParent());try(BufferedWriter w=Files.newBufferedWriter(SAVE)){for(Task t:tasks){w.write(enc(t.title)+"|"+enc(t.subject)+"|"+t.due+"|"+t.priority+"|"+enc(t.notes)+"|"+t.done);w.newLine();}}}catch(Exception e){message("Could not save tasks: "+e.getMessage());}}
    void load(){try{if(Files.exists(SAVE))for(String line:Files.readAllLines(SAVE)){String[] a=line.split("\\|",-1);if(a.length==6)tasks.add(new Task(dec(a[0]),dec(a[1]),a[2],a[3],dec(a[4]),Boolean.parseBoolean(a[5])));}}catch(Exception e){System.err.println("Could not load saved tasks: "+e.getMessage());}}
    void message(String s){JOptionPane.showMessageDialog(this,s,"StudyFlow",JOptionPane.INFORMATION_MESSAGE);}

    JPanel gameMenu(){
        JPanel p=shell("TECHQUEST  /  GAME ZONE","Quick tech challenges • Earn XP • Improve your developer instincts");
        JPanel grid=new JPanel(new GridLayout(1,3,14,0));grid.setOpaque(false);
        grid.add(gameCard("01  CODE ARENA","Java and Python output puzzles, data structures and algorithms.",PURPLE,"Code Arena"));
        grid.add(gameCard("02  ML LAB","Supervised learning, clustering, features, targets and evaluation.",new Color(25,132,151),"ML Lab"));
        grid.add(gameCard("03  DEBUG RUSH","Timed bug hunts. Find the mistake before the clock runs out.",new Color(166,69,97),"Debug Rush"));
        p.add(grid,BorderLayout.CENTER);return p;
    }
    JPanel gameCard(String title,String desc,Color color,String m){JPanel c=card();c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.add(label(title,20,WHITE,true));c.add(Box.createVerticalStrut(12));JTextArea d=new JTextArea(desc);d.setWrapStyleWord(true);d.setLineWrap(true);d.setEditable(false);d.setOpaque(false);d.setForeground(MUTED);d.setFont(NORMAL);d.setRows(4);c.add(d);c.add(Box.createVerticalGlue());JButton b=button("START MISSION  →",color);b.setAlignmentX(LEFT_ALIGNMENT);b.addActionListener(e->startGame(m));c.add(b);return c;}
    JPanel gamePage(){
        JPanel p=shell("MISSION ARENA","Answer correctly before time runs out");
        JPanel center=new JPanel();center.setOpaque(false);center.setLayout(new BoxLayout(center,BoxLayout.Y_AXIS));
        JPanel bar=new JPanel(new BorderLayout());bar.setOpaque(false);timeText=label("TIME  30s",16,new Color(255,197,91),true);scoreText=label("SCORE  0  •  XP  "+xp,16,CYAN,true);bar.add(timeText,BorderLayout.WEST);bar.add(scoreText,BorderLayout.EAST);center.add(bar);center.add(Box.createVerticalStrut(18));
        JPanel question=card();question.setLayout(new BorderLayout());qText=label("Question",22,WHITE,true);question.add(qText,BorderLayout.CENTER);question.setPreferredSize(new Dimension(700,170));question.setMaximumSize(new Dimension(Integer.MAX_VALUE,180));center.add(question);center.add(Box.createVerticalStrut(16));
        options=new JPanel(new GridLayout(0,2,12,12));options.setOpaque(false);center.add(options);center.add(Box.createVerticalStrut(14));feedback=label("Choose the best answer.",14,MUTED,false);center.add(feedback);center.add(Box.createVerticalGlue());
        next=button("NEXT QUESTION  →",PURPLE);next.setAlignmentX(RIGHT_ALIGNMENT);next.setEnabled(false);next.addActionListener(e->{qIndex++;showQuestion();});center.add(next);p.add(center,BorderLayout.CENTER);return p;
    }
    List<Question> questionSet(String m){List<Question> q=new ArrayList<>();
        if(m.equals("Code Arena")){
            q.add(new Question("JAVA","What is printed?  int x=5;  System.out.println(x++ + ++x);",new String[]{"10","11","12","Compilation error"},2,"Post-increment returns 5; pre-increment returns 7. Total = 12."));
            q.add(new Question("PYTHON","What does len([2, 4, 6, 8]) return?",new String[]{"3","4","5","8"},1,"There are four elements in the list."));
            q.add(new Question("DSA","Which data structure follows LIFO?",new String[]{"Queue","Stack","Tree","Graph"},1,"A stack is Last In, First Out."));
            q.add(new Question("JAVA","Which keyword prevents a class from being inherited?",new String[]{"static","private","final","const"},2,"The final keyword prevents subclassing."));
            q.add(new Question("ALGORITHMS","Binary search on sorted data has which time complexity?",new String[]{"O(n)","O(log n)","O(n²)","O(1) always"},1,"The search interval is halved at every step."));
        }else if(m.equals("ML Lab")){
            q.add(new Question("ML","Emails are labelled spam/not spam. Which learning type?",new String[]{"Unsupervised","Supervised","Reinforcement","Clustering"},1,"The data has labels, so it is supervised learning."));
            q.add(new Question("ALGORITHMS","Which can be used for classification?",new String[]{"K-Means","Decision Tree","PCA only","Apriori"},1,"Decision Trees can predict class labels."));
            q.add(new Question("DATA","For house-price prediction, what is usually the target?",new String[]{"House area","Number of rooms","House price","Postal code"},2,"The target is the value the model predicts."));
            q.add(new Question("UNSUPERVISED","Group customers without labels. Which method?",new String[]{"Linear Regression","K-Means","Logistic Regression","Naive Bayes"},1,"K-Means finds groups in unlabelled data."));
            q.add(new Question("METRICS","A simple metric for balanced classification is…",new String[]{"Accuracy","MSE","R-squared","Euclidean distance"},0,"Accuracy is the fraction of correct predictions."));
        }else{
            q.add(new Question("DEBUG","An array has 5 elements. Which loop condition is correct?",new String[]{"i < 5","i <= 5","i = 5","i > 5"},0,"Valid array indexes are 0 through 4."));
            q.add(new Question("PYTHON","What is wrong with: if x = 10: ?",new String[]{"Use == to compare","Use ===","Remove colon","Nothing"},0,"Use == for comparison; = is assignment."));
            q.add(new Question("JAVA","What should be checked before dividing a by b?",new String[]{"b != 0","b = 0","a < 0","a != 0"},0,"Check that the denominator is not zero."));
            q.add(new Question("ARRAYS","An ArrayList has 3 items. Last valid index?",new String[]{"3","2","1","-1"},1,"Indexes start at zero, so size minus one is 2."));
            q.add(new Question("SQL","Which clause filters rows before grouping?",new String[]{"ORDER BY","HAVING","WHERE","GROUP BY"},2,"WHERE filters rows before GROUP BY."));
        }return q;
    }
    void startGame(String m){if(clock!=null)clock.stop();mode=m;questions=questionSet(m);qIndex=0;score=0;navLayout.show(pages,"game");showQuestion();}
    void showQuestion(){if(qIndex>=questions.size()){finishGame();return;}Question q=questions.get(qIndex);answered=false;seconds=mode.equals("Debug Rush")?20:30;
        qText.setText("<html><div style='width:720px'>"+html((qIndex+1)+". "+q.prompt)+"</div></html>");options.removeAll();
        for(int i=0;i<q.choices.length;i++){final int choice=i;JButton b=button((char)('A'+i)+"   "+q.choices[i],PANEL2);b.setHorizontalAlignment(SwingConstants.LEFT);b.addActionListener(e->answer(choice));options.add(b);}
        feedback.setForeground(MUTED);feedback.setText("Choose the best answer. Correct answers earn 100 XP.");timeText.setText("TIME  "+seconds+"s");scoreText.setText("SCORE  "+score+"  •  XP  "+xp);next.setEnabled(false);options.revalidate();options.repaint();
        if(clock!=null)clock.stop();clock=new javax.swing.Timer(1000,e->{seconds--;timeText.setText("TIME  "+seconds+"s");if(seconds<=0&&!answered){answered=true;clock.stop();feedback.setText("Time is up! "+q.explain);disableOptions();next.setEnabled(true);}});clock.start();
    }
    String html(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    void answer(int i){if(answered)return;answered=true;if(clock!=null)clock.stop();Question q=questions.get(qIndex);if(i==q.answer){score+=100;xp+=100;feedback.setForeground(GREEN);feedback.setText("✓ Correct! +100 XP. "+q.explain);}else{feedback.setForeground(RED);feedback.setText("✕ Not quite. "+q.explain);}scoreText.setText("SCORE  "+score+"  •  XP  "+xp);disableOptions();next.setEnabled(true);}
    void disableOptions(){for(Component c:options.getComponents())c.setEnabled(false);}
    void finishGame(){if(clock!=null)clock.stop();String rank=score>=500?"TECH WIZARD":score>=300?"CODE NINJA":score>=100?"BYTE EXPLORER":"ROOKIE";
        JPanel p=shell("MISSION COMPLETE","You finished "+mode);JPanel result=card();result.setLayout(new BoxLayout(result,BoxLayout.Y_AXIS));result.add(label(rank,32,CYAN,true));result.add(Box.createVerticalStrut(12));result.add(label("Score: "+score+"     Total XP: "+xp,20,WHITE,true));result.add(Box.createVerticalStrut(18));
        JButton again=button("PLAY AGAIN",PURPLE);again.addActionListener(e->startGame(mode));JButton back=button("GAME ZONE",new Color(51,65,98));back.addActionListener(e->navLayout.show(pages,"games"));result.add(again);result.add(Box.createVerticalStrut(10));result.add(back);p.add(result,BorderLayout.CENTER);pages.add(p,"result");navLayout.show(pages,"result");}

    JPanel chat(){
        JPanel p=shell("STUDY BUDDY","Offline study helper for programming, machine learning and revision");
        JPanel c=card();c.setLayout(new BorderLayout(10,10));JTextArea log=new JTextArea("STUDY BUDDY\nHi! Ask me about Java, Python, OOP, algorithms, KNN, K-Means, PCA, regression or ML concepts.\n\nNote: This edition uses built-in offline explanations, not a live AI service.\n\n");
        log.setEditable(false);log.setLineWrap(true);log.setWrapStyleWord(true);log.setFont(new Font("Segoe UI",Font.PLAIN,15));log.setForeground(WHITE);log.setBackground(PANEL2);log.setBorder(new EmptyBorder(14,14,14,14));
        JTextField input=new JTextField();styleInput(input);JButton send=button("SEND  ➤",PURPLE);JPanel bottom=new JPanel(new BorderLayout(8,0));bottom.setOpaque(false);bottom.add(input,BorderLayout.CENTER);bottom.add(send,BorderLayout.EAST);
        Runnable submit=()->{String q=input.getText().trim();if(q.isEmpty())return;log.append("YOU: "+q+"\n"+ "BUDDY: "+reply(q)+"\n\n");input.setText("");log.setCaretPosition(log.getDocument().getLength());};send.addActionListener(e->submit.run());input.addActionListener(e->submit.run());
        c.add(new JScrollPane(log),BorderLayout.CENTER);c.add(bottom,BorderLayout.SOUTH);p.add(c,BorderLayout.CENTER);return p;
    }
    String reply(String q){String s=q.toLowerCase();
        if(s.contains("overfit"))return "Overfitting means a model memorises training data and performs poorly on unseen data. Try regularisation, a simpler model, more data or validation.";
        if(s.contains("supervised")||s.contains("unsupervised"))return "Supervised learning uses labelled examples, such as spam/not spam. Unsupervised learning finds patterns without labels, such as customer clusters.";
        if(s.contains("k-means")||s.contains("k means"))return "K-Means: choose K, initialise centroids, assign each point to its nearest centroid, recompute means and repeat until assignments stabilise.";
        if(s.contains("knn"))return "KNN predicts using the K nearest training examples. Classification usually uses majority vote. Scale features when ranges differ.";
        if(s.contains("pca"))return "PCA reduces dimensionality by creating new components that preserve as much data variance as possible.";
        if(s.contains("regression"))return "Regression predicts a numeric value (like price); classification predicts a category (like spam/not spam).";
        if(s.contains("oop")||s.contains("inheritance")||s.contains("polymorphism"))return "OOP has four common pillars: encapsulation, inheritance, polymorphism and abstraction. They help organise reusable, maintainable code.";
        if(s.contains("java"))return "Java is statically typed and object-oriented. Program execution starts at public static void main(String[] args). A class groups fields and methods.";
        if(s.contains("python"))return "Python uses indentation for code blocks, = for assignment and == for comparison. Lists are ordered and mutable; def declares a function.";
        if(s.contains("binary search")||s.contains("algorithm"))return "Binary search works on sorted data by halving the search range each step, giving O(log n) time. An algorithm is a finite sequence of problem-solving steps.";
        if(s.contains("study")||s.contains("plan"))return "Try 25 minutes of focused study followed by a 5-minute break. Set one measurable goal, practise active recall, and start with the nearest deadline.";
        return "I can explain Java, Python, OOP, arrays, loops, algorithms, KNN, K-Means, PCA, regression, classification and overfitting. Ask one specific question or paste a code error. This is an offline helper, not live AI.";
    }

    public static void main(String[] args){SwingUtilities.invokeLater(()->{try{UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());}catch(Exception ignored){}new StudyFlow().setVisible(true);});}
}
