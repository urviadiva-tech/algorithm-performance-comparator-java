import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class AlgorithmComparator extends JFrame {
    static final Color BD=new Color(10,14,26),BP=new Color(17,24,42),BC=new Color(24,34,58);
    static final Color CY=new Color(0,224,255),PU=new Color(163,94,255),GR=new Color(57,255,176);
    static final Color OR=new Color(255,160,50),PI=new Color(255,80,180),TX=new Color(220,235,255);
    static final Color TM=new Color(110,130,175),BO=new Color(40,55,90);
    static final Color[] AC={CY,PU,GR,OR,PI,new Color(255,220,80)};
    static final Font FT=new Font("Monospaced",Font.BOLD,24),FH=new Font("Monospaced",Font.BOLD,15);
    static final Font FB=new Font("Monospaced",Font.PLAIN,13),FS=new Font("Monospaced",Font.PLAIN,12);
    static final String[] AN={"Bubble Sort","Selection Sort","Insertion Sort","Merge Sort","Quick Sort","Arrays.sort (Tim)"};

    List<BR> results=new ArrayList<>();
    JPanel chart; JTable table; DefaultTableModel model;
    JTextArea log; JLabel status,clock,szLbl,dataSourceLbl;
    JSlider slider; JComboBox<String> dtCombo,ctCombo;
    JCheckBox[] boxes; JProgressBar bar;
    volatile boolean running=false;
    int[] uploadedData=null;

    public AlgorithmComparator(){
        super("⚡ Algorithm Performance Comparator  |  Core Java Project");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1350,850); setLocationRelativeTo(null);
        buildUI(); startClock(); setVisible(true);
    }

    void buildUI(){
        JPanel root=new JPanel(new BorderLayout());
        root.setBackground(BD);
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.add(topBar(),BorderLayout.NORTH);
        root.add(center(),BorderLayout.CENTER);
        root.add(statusBar(),BorderLayout.SOUTH);
        setContentPane(root);
    }

    JPanel topBar(){
        JPanel bar=new JPanel(new BorderLayout());
        bar.setBackground(new Color(0,40,70));
        bar.setBorder(new CompoundBorder(new MatteBorder(0,0,3,0,CY),
                BorderFactory.createEmptyBorder(10,20,10,20)));
        JPanel left=tp(); left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        JLabel title=new JLabel("⚡  ALGORITHM PERFORMANCE COMPARATOR");
        title.setFont(FT); title.setForeground(CY);
        JLabel sub=new JLabel("   Core Java  ·  Sorting  ·  Concurrency  ·  Collections  ·  Streams");
        sub.setFont(FS); sub.setForeground(new Color(180,220,255));
        left.add(title); left.add(Box.createVerticalStrut(3)); left.add(sub);
        clock=new JLabel("00:00:00");
        clock.setFont(new Font("Monospaced",Font.BOLD,22)); clock.setForeground(GR);
        bar.add(left,BorderLayout.WEST); bar.add(clock,BorderLayout.EAST);
        return bar;
    }

    JSplitPane center(){
        JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftPanel(),rightPanel());
        sp.setDividerLocation(320); sp.setDividerSize(4);
        sp.setBackground(BD); sp.setBorder(null); return sp;
    }

    JPanel leftPanel(){
        JPanel inner=new JPanel(); inner.setBackground(BP);
        inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));

        inner.add(lbl("▸ ARRAY SIZE",FH,CY)); inner.add(vs(3));
        inner.add(sliderPanel()); inner.add(vs(8));

        inner.add(lbl("▸ DATA TYPE",FH,CY)); inner.add(vs(3));
        dtCombo=combo(new String[]{"Random","Nearly Sorted","Reverse Sorted","All Same","Sawtooth Pattern","── None (Using File) ──"});
        dtCombo.addActionListener(e->{
            String sel=(String)dtCombo.getSelectedItem();
            if(!"── None (Using File) ──".equals(sel)){
                uploadedData=null;
                dataSourceLbl.setText("  Source: Generated");
                dataSourceLbl.setForeground(TM);
            }
        });
        inner.add(dtCombo); inner.add(vs(4));

        JButton uploadBtn=btn("📂  UPLOAD DATA FILE",new Color(255,200,0),BD);
        uploadBtn.addActionListener(e->uploadFile());
        inner.add(uploadBtn); inner.add(vs(3));

        dataSourceLbl=new JLabel("  Source: Generated");
        dataSourceLbl.setFont(FS); dataSourceLbl.setForeground(TM);
        dataSourceLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(dataSourceLbl); inner.add(vs(8));

        inner.add(lbl("▸ SELECT ALGORITHMS",FH,CY)); inner.add(vs(3));
        boxes=new JCheckBox[AN.length];
        for(int i=0;i<AN.length;i++){
            boxes[i]=chk(AN[i],AC[i]); boxes[i].setSelected(true);
            inner.add(boxes[i]); inner.add(vs(1));
        }
        inner.add(vs(8));

        inner.add(lbl("▸ CHART TYPE",FH,CY)); inner.add(vs(3));
        ctCombo=combo(new String[]{"Bar Chart","Line Chart","Horizontal Bar"});
        ctCombo.addActionListener(e->chart.repaint());
        inner.add(ctCombo); inner.add(vs(10));

        JButton run=btn("▶  RUN BENCHMARK",CY,BD);
        JButton clr=btn("✕  CLEAR RESULTS",new Color(255,80,80),BD);
        JButton exp=btn("↓  EXPORT LOG",GR,BD);
        JButton inf=btn("ⓘ  COMPLEXITY INFO",PU,BD);
        JButton viz=btn("👁  VIEW INTERNAL WORKING",new Color(255,140,0),BD);
        run.addActionListener(e->runBench());
        clr.addActionListener(e->clear());
        exp.addActionListener(e->export());
        inf.addActionListener(e->showInfo());
        viz.addActionListener(e->showInternalWorking());
        for(JButton b:new JButton[]{run,clr,exp,inf,viz}){inner.add(b);inner.add(vs(4));}

        bar=new JProgressBar(0,100); bar.setStringPainted(true); bar.setString("Ready");
        bar.setForeground(CY); bar.setBackground(BC); bar.setFont(FS);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE,22));
        inner.add(vs(2)); inner.add(bar);

        JPanel outer=new JPanel(new BorderLayout());
        outer.setBackground(BP);
        outer.setBorder(new MatteBorder(0,0,0,2,BO));
        outer.add(inner,BorderLayout.NORTH);
        return outer;
    }

    JPanel rightPanel(){
        JPanel p=new JPanel(new BorderLayout(0,4)); p.setBackground(BD);
        chart=new ChartPanel();
        String[] cols={"Algorithm","Array Size","Data Type","Time (ms)","Comparisons","Swaps","Status"};
        model=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(model); styleTable(table);
        JScrollPane ts=dScroll(table); ts.setBorder(tBorder("BENCHMARK RESULTS"));
        log=new JTextArea(); log.setBackground(new Color(8,12,22));
        log.setForeground(GR); log.setFont(FS); log.setEditable(false); log.setLineWrap(true);
        JScrollPane ls=dScroll(log); ls.setBorder(tBorder("EXECUTION LOG"));
        JSplitPane bt=new JSplitPane(JSplitPane.VERTICAL_SPLIT,ts,ls);
        bt.setDividerLocation(160); bt.setDividerSize(4); bt.setBorder(null);
        bt.setPreferredSize(new Dimension(0,320));
        p.add(chart,BorderLayout.CENTER); p.add(bt,BorderLayout.SOUTH); return p;
    }

    JPanel statusBar(){
        JPanel b=new JPanel(new BorderLayout()); b.setBackground(new Color(8,12,22));
        b.setBorder(new MatteBorder(2,0,0,0,BO)); b.setPreferredSize(new Dimension(0,28));
        status=lbl("  ✔  Ready — Select algorithms and click RUN BENCHMARK",FS,TM);
        JLabel r=lbl("Core Java · Swing GUI · Multithreading · Collections · Streams   ",FS,new Color(60,80,120));
        b.add(status,BorderLayout.WEST); b.add(r,BorderLayout.EAST); return b;
    }

    JPanel sliderPanel(){
        JPanel p=tp(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        slider=new JSlider(100,20000,5000); slider.setBackground(BP); slider.setForeground(CY);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        szLbl=lbl("Array Size:  5,000",FB,CY); szLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.addChangeListener(e->{
            szLbl.setText(String.format("Array Size:  %,d",slider.getValue()));
            uploadedData=null;
            dataSourceLbl.setText("  Source: Generated");
            dataSourceLbl.setForeground(TM);
            dtCombo.setSelectedIndex(0);
        });
        p.add(szLbl); p.add(vs(3)); p.add(slider);
        JPanel btns=tp(); btns.setLayout(new FlowLayout(FlowLayout.LEFT,3,0));
        for(int sz:new int[]{500,1000,5000,10000,20000}){
            JButton b=new JButton(sz>=1000?(sz/1000)+"K":""+sz);
            b.setFont(FS); b.setBackground(BC); b.setForeground(TX);
            b.setBorder(BorderFactory.createLineBorder(BO)); b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int s=sz; b.addActionListener(e->{
                slider.setValue(s); uploadedData=null;
                dataSourceLbl.setText("  Source: Generated");
                dataSourceLbl.setForeground(TM);
                dtCombo.setSelectedIndex(0);
            });
            btns.add(b);
        }
        p.add(vs(3)); p.add(btns); return p;
    }

    void uploadFile(){
	    JFileChooser fc=new JFileChooser();
	    fc.setDialogTitle("Select a data file (.txt or .csv)");
	    fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text/CSV files","txt","csv"));
	    int result=fc.showOpenDialog(this);
	    if(result!=JFileChooser.APPROVE_OPTION) return;
	    File f=fc.getSelectedFile();
	    if(f==null||!f.exists()){
	        JOptionPane.showMessageDialog(this,"File not found!","Error",JOptionPane.ERROR_MESSAGE);
	        return;
	    }
	    try{
	        // Use BufferedReader instead of Files.readAllBytes — more compatible
	        StringBuilder sb=new StringBuilder();
	        BufferedReader br=new BufferedReader(new FileReader(f));
	        String line;
	        while((line=br.readLine())!=null){
	            sb.append(line).append(",");
	        }
	        br.close();

	        String content=sb.toString().trim();
	        if(content.isEmpty()){
	            JOptionPane.showMessageDialog(this,"File is empty!","Error",JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        // Split by comma, space, newline, or semicolon
	        String[] tokens=content.split("[,;\\s]+");
	        List<Integer> nums=new ArrayList<>();
	        int skipped=0;
	        for(String t:tokens){
	            t=t.trim();
	            if(t.isEmpty()) continue;
	            try{ nums.add(Integer.parseInt(t)); }
	            catch(NumberFormatException ex){ skipped++; }
	        }

	        if(nums.isEmpty()){
	            JOptionPane.showMessageDialog(this,
	                "No valid integers found!\n\nMake sure your file has numbers like:\n45, 23, 89, 12\nor one number per line.",
	                "Error",JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        uploadedData=nums.stream().mapToInt(Integer::intValue).toArray();
	        szLbl.setText(String.format("Array Size:  %,d",uploadedData.length));
	        dataSourceLbl.setText("  ✔ "+f.getName()+" ("+uploadedData.length+" numbers)");
	        dataSourceLbl.setForeground(GR);
	        dtCombo.setSelectedItem("── None (Using File) ──");

	        String msg="✔  File loaded successfully!\n\nFile: "+f.getName()+
	                   "\nNumbers loaded: "+uploadedData.length;
	        if(skipped>0) msg+="\nNon-numeric values skipped: "+skipped;
	        msg+="\n\nClick RUN BENCHMARK to sort your data.";
	        JOptionPane.showMessageDialog(this,msg,"File Loaded",JOptionPane.INFORMATION_MESSAGE);
	        addLog("  [UPLOAD] "+f.getName()+" → "+uploadedData.length+" integers loaded");

	    }catch(IOException ex){
	        JOptionPane.showMessageDialog(this,
	            "Could not read file:\n"+ex.getMessage()+
	            "\n\nMake sure the file is not open in another program.",
	            "Read Error",JOptionPane.ERROR_MESSAGE);
	    }
}

    void showInternalWorking(){
        JDialog dlg=new JDialog(this,"👁  Internal Working — Step by Step Visualization",true);
        dlg.setSize(900,650); dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BD);

        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT,10,8));
        top.setBackground(new Color(0,40,70));
        top.setBorder(new MatteBorder(0,0,2,0,CY));

        JLabel algLbl=new JLabel("Algorithm:"); algLbl.setFont(FB); algLbl.setForeground(CY);
        JComboBox<String> algSel=new JComboBox<>(AN);
        algSel.setBackground(BC); algSel.setForeground(TX); algSel.setFont(FB);

        JLabel sizeLbl2=new JLabel("Size:"); sizeLbl2.setFont(FB); sizeLbl2.setForeground(CY);
        JComboBox<String> sizeSel=new JComboBox<>(new String[]{"10","15","20","25"});
        sizeSel.setBackground(BC); sizeSel.setForeground(TX); sizeSel.setFont(FB);

        JButton startViz=new JButton("▶  START");
        startViz.setBackground(CY); startViz.setForeground(BD); startViz.setFont(FB); startViz.setFocusPainted(false);

        JButton stepBtn=new JButton("⏭  NEXT STEP");
        stepBtn.setBackground(OR); stepBtn.setForeground(BD); stepBtn.setFont(FB);
        stepBtn.setFocusPainted(false); stepBtn.setEnabled(false);

        JButton autoBtn=new JButton("⚡  AUTO PLAY");
        autoBtn.setBackground(GR); autoBtn.setForeground(BD); autoBtn.setFont(FB);
        autoBtn.setFocusPainted(false); autoBtn.setEnabled(false);

        top.add(algLbl); top.add(algSel); top.add(sizeLbl2); top.add(sizeSel);
        top.add(startViz); top.add(stepBtn); top.add(autoBtn);

        JPanel vizArea=new JPanel(new BorderLayout());
        vizArea.setBackground(BD);
        ArrayVizPanel arrayPanel=new ArrayVizPanel();
        arrayPanel.setPreferredSize(new Dimension(0,300));

        JTextArea stepInfo=new JTextArea(6,0);
        stepInfo.setBackground(new Color(8,12,22)); stepInfo.setForeground(GR);
        stepInfo.setFont(FS); stepInfo.setEditable(false); stepInfo.setLineWrap(true);
        stepInfo.setText("  Select an algorithm and size, then click START.\n\n"+
                         "  Color guide:\n"+
                         "  Yellow = Comparing two elements\n"+
                         "  Red    = Swapping two elements\n"+
                         "  Green  = Element in sorted position");
        JScrollPane stepScroll=new JScrollPane(stepInfo);
        stepScroll.setBorder(tBorder("STEP EXPLANATION"));
        stepScroll.setPreferredSize(new Dimension(0,180));

        vizArea.add(arrayPanel,BorderLayout.CENTER);
        vizArea.add(stepScroll,BorderLayout.SOUTH);

        final List<int[]> snapshots=new ArrayList<>();
        final List<int[]> highlights=new ArrayList<>();
        final List<String> explanations=new ArrayList<>();
        final int[] stepIdx={0};
        final javax.swing.Timer[] autoTimer={null};

        startViz.addActionListener(e->{
            snapshots.clear(); highlights.clear(); explanations.clear(); stepIdx[0]=0;
            if(autoTimer[0]!=null){autoTimer[0].stop(); autoTimer[0]=null; autoBtn.setText("⚡  AUTO PLAY");}
            int sz=Integer.parseInt((String)sizeSel.getSelectedItem());
            int[] arr=new int[sz]; Random rnd=new Random(99);
            for(int i=0;i<sz;i++) arr[i]=rnd.nextInt(99)+1;
            generateSteps(arr.clone(),algSel.getSelectedIndex(),snapshots,highlights,explanations);
            stepBtn.setEnabled(true); autoBtn.setEnabled(true);
            stepIdx[0]=0;
            if(!snapshots.isEmpty()){
                arrayPanel.update(snapshots.get(0),highlights.get(0),0,snapshots.size());
                stepInfo.setText(explanations.get(0));
            }
        });

        stepBtn.addActionListener(e->{
            if(stepIdx[0]<snapshots.size()-1){
                stepIdx[0]++;
                arrayPanel.update(snapshots.get(stepIdx[0]),highlights.get(stepIdx[0]),stepIdx[0],snapshots.size());
                stepInfo.setText(explanations.get(stepIdx[0]));
            }
        });

        autoBtn.addActionListener(e->{
            if(autoTimer[0]!=null&&autoTimer[0].isRunning()){
                autoTimer[0].stop(); autoBtn.setText("⚡  AUTO PLAY"); return;
            }
            autoBtn.setText("⏸  PAUSE");
            autoTimer[0]=new javax.swing.Timer(600,ev->{
                if(stepIdx[0]<snapshots.size()-1){
                    stepIdx[0]++;
                    arrayPanel.update(snapshots.get(stepIdx[0]),highlights.get(stepIdx[0]),stepIdx[0],snapshots.size());
                    stepInfo.setText(explanations.get(stepIdx[0]));
                }else{
                    autoTimer[0].stop(); autoBtn.setText("⚡  AUTO PLAY");
                    stepInfo.setText("  ✔  SORTING COMPLETE!\n\n"+explanations.get(stepIdx[0]));
                }
            });
            autoTimer[0].start();
        });

        dlg.add(top,BorderLayout.NORTH);
        dlg.add(vizArea,BorderLayout.CENTER);
        dlg.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){if(autoTimer[0]!=null)autoTimer[0].stop();}
        });
        dlg.setVisible(true);
    }

    void generateSteps(int[]arr,int algIdx,List<int[]>snaps,List<int[]>highs,List<String>exps){
        snaps.add(arr.clone()); highs.add(new int[]{-1,-1,0});
        exps.add("  Initial array — "+arr.length+" elements\n  Algorithm: "+AN[algIdx]+"\n  Press NEXT STEP or AUTO PLAY to begin.");
        switch(algIdx){
            case 0: vizBubble(arr,snaps,highs,exps); break;
            case 1: vizSelection(arr,snaps,highs,exps); break;
            case 2: vizInsertion(arr,snaps,highs,exps); break;
            case 3: vizMerge(arr,0,arr.length-1,snaps,highs,exps); break;
            case 4: vizQuick(arr,0,arr.length-1,snaps,highs,exps); break;
            case 5:
                Arrays.sort(arr);
                snaps.add(arr.clone()); highs.add(new int[]{-1,-1,2});
                exps.add("  Arrays.sort (TimSort) — Java's built-in sort\n  Hybrid of Merge Sort + Insertion Sort\n  Detects natural runs and merges efficiently.\n  Result: Sorted in O(n log n) time.");
                break;
        }
        snaps.add(arr.clone()); highs.add(new int[]{-2,-2,2});
        exps.add("  ✔  SORTING COMPLETE!\n  Algorithm: "+AN[algIdx]+"\n  Total steps: "+snaps.size());
    }

    void vizBubble(int[]a,List<int[]>s,List<int[]>h,List<String>e){
        for(int i=0;i<a.length-1;i++)
            for(int j=0;j<a.length-1-i;j++){
                s.add(a.clone()); h.add(new int[]{j,j+1,0});
                e.add(String.format("  BUBBLE SORT — Pass %d\n  Comparing a[%d]=%d with a[%d]=%d\n  %s",i+1,j,a[j],j+1,a[j+1],a[j]>a[j+1]?"→ SWAP needed":"→ No swap"));
                if(a[j]>a[j+1]){
                    int t=a[j];a[j]=a[j+1];a[j+1]=t;
                    s.add(a.clone()); h.add(new int[]{j,j+1,1});
                    e.add(String.format("  BUBBLE SORT — SWAPPED\n  a[%d] and a[%d] swapped\n  Larger element bubbling right",j,j+1));
                }
            }
    }

    void vizSelection(int[]a,List<int[]>s,List<int[]>h,List<String>e){
        for(int i=0;i<a.length-1;i++){
            int min=i;
            for(int j=i+1;j<a.length;j++){
                s.add(a.clone()); h.add(new int[]{min,j,0});
                e.add(String.format("  SELECTION SORT — Finding minimum\n  Current min: a[%d]=%d\n  Comparing with a[%d]=%d\n  %s",min,a[min],j,a[j],a[j]<a[min]?"→ New minimum found!":"→ Keep current minimum"));
                if(a[j]<a[min]) min=j;
            }
            if(min!=i){
                s.add(a.clone()); h.add(new int[]{i,min,1});
                e.add(String.format("  SELECTION SORT — SWAP\n  Placing minimum %d at position %d",a[min],i));
                int t=a[i];a[i]=a[min];a[min]=t;
                s.add(a.clone()); h.add(new int[]{i,i,2});
                e.add(String.format("  SELECTION SORT — Position %d sorted\n  Value %d placed correctly",i,a[i]));
            }
        }
    }

    void vizInsertion(int[]a,List<int[]>s,List<int[]>h,List<String>e){
        for(int i=1;i<a.length;i++){
            int key=a[i],j=i-1;
            s.add(a.clone()); h.add(new int[]{i,i,0});
            e.add(String.format("  INSERTION SORT — Key picked\n  Key = a[%d] = %d\n  Will insert into sorted left part [0..%d]",i,key,i-1));
            while(j>=0&&a[j]>key){
                s.add(a.clone()); h.add(new int[]{j,j+1,1});
                e.add(String.format("  INSERTION SORT — Shifting\n  a[%d]=%d > key=%d, shift right",j,a[j],key));
                a[j+1]=a[j]; j--;
            }
            a[j+1]=key;
            s.add(a.clone()); h.add(new int[]{j+1,j+1,2});
            e.add(String.format("  INSERTION SORT — Inserted!\n  Key=%d placed at position %d",key,j+1));
        }
    }

    void vizMerge(int[]a,int l,int r,List<int[]>s,List<int[]>h,List<String>e){
        if(l<r){
            int m=(l+r)/2;
            s.add(a.clone()); h.add(new int[]{l,r,0});
            e.add(String.format("  MERGE SORT — Dividing [%d..%d]\n  Left:[%d..%d]  Right:[%d..%d]",l,r,l,m,m+1,r));
            vizMerge(a,l,m,s,h,e); vizMerge(a,m+1,r,s,h,e);
            int[]tmp=Arrays.copyOfRange(a,l,r+1);
            int i=0,j=m-l+1,k=l;
            while(i<=m-l&&j<=r-l){
                s.add(a.clone()); h.add(new int[]{l+i,l+j,0});
                e.add(String.format("  MERGE SORT — Merging [%d..%d]\n  Comparing %d with %d, taking smaller",l,r,tmp[i],tmp[j]));
                if(tmp[i]<=tmp[j])a[k++]=tmp[i++];
                else{a[k++]=tmp[j++];
                    s.add(a.clone()); h.add(new int[]{k-1,k-1,1});
                    e.add("  MERGE SORT — Took from right half");}
            }
            while(i<=m-l)a[k++]=tmp[i++];
            while(j<=r-l)a[k++]=tmp[j++];
            s.add(a.clone()); h.add(new int[]{l,r,2});
            e.add(String.format("  MERGE SORT — Merged [%d..%d] ✔",l,r));
        }
    }

    void vizQuick(int[]a,int l,int r,List<int[]>s,List<int[]>h,List<String>e){
        if(l<r){
            int pv=a[r];
            s.add(a.clone()); h.add(new int[]{r,r,0});
            e.add(String.format("  QUICK SORT — Pivot = a[%d] = %d\n  Elements < %d go left, > %d go right",r,pv,pv,pv));
            int i=l-1;
            for(int j=l;j<r;j++){
                s.add(a.clone()); h.add(new int[]{j,r,0});
                e.add(String.format("  QUICK SORT — Comparing a[%d]=%d with pivot=%d\n  %s",j,a[j],pv,a[j]<=pv?"→ <= pivot, move to left":"→ > pivot, stays right"));
                if(a[j]<=pv){i++;
                    if(i!=j){
                        s.add(a.clone()); h.add(new int[]{i,j,1});
                        e.add(String.format("  QUICK SORT — SWAP a[%d]=%d with a[%d]=%d",i,a[i],j,a[j]));
                        int t=a[i];a[i]=a[j];a[j]=t;
                    }
                }
            }
            int t=a[i+1];a[i+1]=a[r];a[r]=t;
            s.add(a.clone()); h.add(new int[]{i+1,i+1,2});
            e.add(String.format("  QUICK SORT — Pivot %d placed at final position %d ✔",pv,i+1));
            int pi=i+1;
            vizQuick(a,l,pi-1,s,h,e); vizQuick(a,pi+1,r,s,h,e);
        }
    }

    class ArrayVizPanel extends JPanel{
        int[]arr=new int[0]; int[]hl=new int[]{-1,-1,0}; int step=0,total=0;
        ArrayVizPanel(){setBackground(BC);setBorder(tBorder("ARRAY VISUALIZATION"));}
        void update(int[]a,int[]h,int s,int t){arr=a.clone();hl=h;step=s;total=t;repaint();}
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(arr.length==0){g2.setColor(TM);g2.setFont(FH);g2.drawString("Select algorithm and click START",80,getHeight()/2);return;}
            int pad=40,top=30,bottom=60,chartW=getWidth()-pad*2,chartH=getHeight()-top-bottom;
            int barW=Math.max(4,(chartW-arr.length*2)/arr.length);
            int maxVal=Arrays.stream(arr).max().orElse(1);
            g2.setColor(new Color(30,50,80)); g2.fillRect(pad,top-20,chartW,8);
            g2.setColor(CY); g2.fillRect(pad,top-20,(int)((double)step/Math.max(1,total)*chartW),8);
            g2.setFont(FS); g2.setColor(TX); g2.drawString("Step "+step+" / "+total,pad,top-25);
            for(int i=0;i<arr.length;i++){
                int barH=(int)((double)arr[i]/maxVal*chartH);
                int x=pad+i*(barW+2),y=top+chartH-barH;
                Color bc=new Color(50,100,150);
                if(hl[0]==-2) bc=GR;
                else if(i==hl[0]||i==hl[1]) bc=hl[2]==0?new Color(255,220,0):hl[2]==1?new Color(255,60,60):GR;
                g2.setColor(bc); g2.fillRoundRect(x,y,barW,barH,3,3);
                if(barW>=18){
                    g2.setColor(BD); g2.setFont(new Font("Monospaced",Font.BOLD,10));
                    FontMetrics fm=g2.getFontMetrics(); String val=""+arr[i];
                    g2.drawString(val,x+(barW-fm.stringWidth(val))/2,y+barH-3);
                }
            }
            int lx=pad,ly=top+chartH+15; g2.setFont(FS);
            drawLegend(g2,lx,ly,new Color(255,220,0),"Comparing");
            drawLegend(g2,lx+130,ly,new Color(255,60,60),"Swapping");
            drawLegend(g2,lx+260,ly,GR,"Sorted");
            drawLegend(g2,lx+390,ly,new Color(50,100,150),"Unsorted");
        }
        void drawLegend(Graphics2D g2,int x,int y,Color c,String label){
            g2.setColor(c);g2.fillRect(x,y-10,14,14);g2.setColor(TX);g2.drawString(label,x+18,y+2);
        }
    }

    void runBench(){
        if(running)return;
        List<Integer> sel=new ArrayList<>();
        for(int i=0;i<boxes.length;i++) if(boxes[i].isSelected()) sel.add(i);
        if(sel.isEmpty()){JOptionPane.showMessageDialog(this,"Select at least one algorithm!","Warning",JOptionPane.WARNING_MESSAGE);return;}
        int size=uploadedData!=null?uploadedData.length:slider.getValue();
        String dt=uploadedData!=null?"User File":((String)dtCombo.getSelectedItem());
        running=true; bar.setValue(0); bar.setString("Running...");
        setSt("⏳  Benchmarking "+sel.size()+" algorithms on "+String.format("%,d",size)+" elements...");
        new SwingWorker<List<BR>,String>(){
            protected List<BR> doInBackground() throws Exception{
                List<BR> res=new ArrayList<>();
                ExecutorService ex=Executors.newFixedThreadPool(Math.min(sel.size(),Runtime.getRuntime().availableProcessors()));
                List<Future<BR>> fs=new ArrayList<>();
                for(int idx:sel){final int ai=idx;
                    fs.add(ex.submit(()->{
                        publish("  ["+AN[ai]+"] Starting...");
                        int[]data=uploadedData!=null?uploadedData.clone():generateData(size,dt);
                        BR r=runAlg(ai,data,data.length,dt);
                        publish("  ["+AN[ai]+"] Done → "+r.ms+" ms"); return r;
                    }));
                }
                int done=0;
                for(Future<BR> f:fs){
                    try{res.add(f.get());}catch(ExecutionException e){publish("  ERROR: "+e.getMessage());}
                    final int pct=(++done*100)/fs.size();
                    SwingUtilities.invokeLater(()->bar.setValue(pct));
                }
                ex.shutdown();
                return res.stream().sorted(Comparator.comparingLong(r->r.ms)).collect(Collectors.toList());
            }
            protected void process(List<String> c){c.forEach(m->addLog(m));}
            protected void done(){
                try{
                    List<BR> nr=get(); results.clear(); results.addAll(nr);
                    refreshTable(); chart.repaint();
                    nr.stream().min(Comparator.comparingLong(r->r.ms))
                      .ifPresent(r->setSt("✔  Complete! Fastest: "+r.name+" ("+r.ms+" ms)"));
                    bar.setValue(100); bar.setString("Complete!");
                    addLog("\n  ══ BENCHMARK COMPLETE ══");
                    addLog("  Timestamp: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    LongSummaryStatistics st=nr.stream().mapToLong(r->r.ms).summaryStatistics();
                    addLog(String.format("  Min: %d ms | Max: %d ms | Avg: %.1f ms",st.getMin(),st.getMax(),st.getAverage()));
                }catch(Exception e){setSt("✘  Error: "+e.getMessage());}
                running=false;
            }
        }.execute();
        addLog("\n  ══ NEW BENCHMARK ══  "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        addLog("  Data: "+dt+"  |  Size: "+String.format("%,d",size));
    }

    int[] generateData(int size,String type){
        int[]a=new int[size]; Random r=new Random(42);
        switch(type){
            case "Random": for(int i=0;i<size;i++) a[i]=r.nextInt(size*10); break;
            case "Nearly Sorted": for(int i=0;i<size;i++) a[i]=i;
                for(int i=0;i<size/20;i++){int x=r.nextInt(size),y=r.nextInt(size),t=a[x];a[x]=a[y];a[y]=t;} break;
            case "Reverse Sorted": for(int i=0;i<size;i++) a[i]=size-i; break;
            case "All Same": Arrays.fill(a,42); break;
            case "Sawtooth Pattern": for(int i=0;i<size;i++) a[i]=i%100; break;
            default: for(int i=0;i<size;i++) a[i]=r.nextInt(size);
        }
        return a;
    }

    BR runAlg(int idx,int[]orig,int size,String dt){
        int[]a=Arrays.copyOf(orig,orig.length); long c=0,s=0;
        Instant st=Instant.now();
        switch(idx){
            case 0: for(int i=0;i<a.length-1;i++) for(int j=0;j<a.length-1-i;j++){c++;if(a[j]>a[j+1]){int t=a[j];a[j]=a[j+1];a[j+1]=t;s++;}} break;
            case 1: for(int i=0;i<a.length-1;i++){int m=i;for(int j=i+1;j<a.length;j++){c++;if(a[j]<a[m])m=j;}if(m!=i){int t=a[i];a[i]=a[m];a[m]=t;s++;}} break;
            case 2: for(int i=1;i<a.length;i++){int k=a[i],j=i-1;while(j>=0&&a[j]>k){c++;a[j+1]=a[j];j--;s++;}a[j+1]=k;} break;
            case 3: {long[]cc={0};long[]ss={0};mSort(a,0,a.length-1,cc,ss);c=cc[0];s=ss[0];} break;
            case 4: {long[]cc={0};long[]ss={0};qSort(a,0,a.length-1,cc,ss);c=cc[0];s=ss[0];} break;
            case 5: Arrays.sort(a);c=(long)(a.length*Math.log(a.length)/Math.log(2));s=c/2; break;
        }
        return new BR(AN[idx],size,dt,Math.max(1,Duration.between(st,Instant.now()).toMillis()),c,s,idx);
    }

    void mSort(int[]a,int l,int r,long[]c,long[]s){
        if(l<r){int m=(l+r)/2;mSort(a,l,m,c,s);mSort(a,m+1,r,c,s);
            int[]t=Arrays.copyOfRange(a,l,r+1);int i=0,j=m-l+1,k=l;
            while(i<=m-l&&j<=r-l){c[0]++;if(t[i]<=t[j])a[k++]=t[i++];else{a[k++]=t[j++];s[0]++;}}
            while(i<=m-l)a[k++]=t[i++];while(j<=r-l)a[k++]=t[j++];}
    }

    void qSort(int[]a,int l,int r,long[]c,long[]s){
        if(l<r){int pv=a[r],i=l-1;
            for(int j=l;j<r;j++){c[0]++;if(a[j]<=pv){i++;int t=a[i];a[i]=a[j];a[j]=t;s[0]++;}}
            int t=a[i+1];a[i+1]=a[r];a[r]=t;s[0]++;int pi=i+1;
            qSort(a,l,pi-1,c,s);qSort(a,pi+1,r,c,s);}
    }

    void refreshTable(){
        model.setRowCount(0);
        results.forEach(r->model.addRow(new Object[]{r.name,String.format("%,d",r.size),r.dt,r.ms,String.format("%,d",r.comps),String.format("%,d",r.swaps),"✔ Done"}));
    }

    void clear(){
        results.clear(); model.setRowCount(0); chart.repaint();
        log.setText(""); bar.setValue(0); bar.setString("Ready");
        uploadedData=null;
        dataSourceLbl.setText("  Source: Generated");
        dataSourceLbl.setForeground(TM);
        dtCombo.setSelectedIndex(0);
        setSt("  ✔  Cleared. Ready for new benchmark.");
    }

    void export(){
        try{
            String fn="benchmark_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))+".txt";
            java.io.PrintWriter pw=new java.io.PrintWriter(new java.io.FileWriter(fn));
            pw.println("=== ALGORITHM PERFORMANCE COMPARATOR REPORT ===");
            pw.println("Generated: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pw.println();
            results.forEach(r->{pw.printf("Algorithm: %s | Size: %,d | Time: %d ms | Comparisons: %,d | Swaps: %,d%n",r.name,r.size,r.ms,r.comps,r.swaps);pw.println("---");});
            pw.println(log.getText()); pw.close();
            JOptionPane.showMessageDialog(this,"✔  Exported to:\n"+new java.io.File(fn).getAbsolutePath(),"Export Successful",JOptionPane.INFORMATION_MESSAGE);
            addLog("  [EXPORT] Saved → "+fn);
        }catch(Exception e){JOptionPane.showMessageDialog(this,"Export failed: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    void showInfo(){
        String[][]d={{"Bubble Sort","O(n²)","O(n²)","O(1)","Stable"},
            {"Selection Sort","O(n²)","O(n²)","O(1)","Unstable"},
            {"Insertion Sort","O(n)","O(n²)","O(1)","Stable"},
            {"Merge Sort","O(n log n)","O(n log n)","O(n)","Stable"},
            {"Quick Sort","O(n log n)","O(n²)","O(log n)","Unstable"},
            {"Arrays.sort","O(n log n)","O(n log n)","O(n)","Stable (TimSort)"}};
        JDialog dl=new JDialog(this,"Big-O Complexity Reference",true);
        dl.setSize(720,300); dl.setLocationRelativeTo(this); dl.getContentPane().setBackground(BP);
        DefaultTableModel m=new DefaultTableModel(d,new String[]{"Algorithm","Best","Worst","Space","Stable?"}){public boolean isCellEditable(int r,int c){return false;}};
        JTable t=new JTable(m); styleTable(t); t.setRowHeight(34);
        JLabel h=new JLabel("  ⚡  Time & Space Complexity — Big-O Reference");
        h.setFont(FH); h.setForeground(CY); h.setOpaque(true);
        h.setBackground(BC); h.setPreferredSize(new Dimension(0,42));
        dl.add(h,BorderLayout.NORTH); dl.add(dScroll(t),BorderLayout.CENTER); dl.setVisible(true);
    }

    void startClock(){
        new javax.swing.Timer(1000,e->clock.setText(
            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))).start();
    }

    class ChartPanel extends JPanel{
        ChartPanel(){setBackground(BC);setBorder(tBorder("PERFORMANCE CHART"));}
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(results.isEmpty()){
                g2.setColor(new Color(30,45,75));
                for(int i=0;i<getWidth();i+=40)g2.drawLine(i,0,i,getHeight());
                for(int i=0;i<getHeight();i+=40)g2.drawLine(0,i,getWidth(),i);
                g2.setFont(FH); g2.setColor(TM);
                String msg="Run a benchmark to see results here...";
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(msg,(getWidth()-fm.stringWidth(msg))/2,getHeight()/2); return;
            }
            String ct=(String)ctCombo.getSelectedItem();
            if("Line Chart".equals(ct)) lineChart(g2);
            else if("Horizontal Bar".equals(ct)) hBar(g2);
            else barChart(g2);
        }
        void barChart(Graphics2D g2){
            int pad=80,top=50,gap=8,cw=getWidth()-pad-20,ch=getHeight()-top-60;
            long mx=results.stream().mapToLong(r->r.ms).max().orElse(1);
            grid(g2,pad,top,cw,ch,mx);
            int bw=(cw-gap*(results.size()+1))/results.size();
            for(int i=0;i<results.size();i++){
                BR r=results.get(i); int bh=(int)((double)r.ms/mx*ch);
                int x=pad+gap+i*(bw+gap),y=top+ch-bh;
                g2.setPaint(new GradientPaint(x,y,br(AC[r.ai]),x,y+bh,dk(AC[r.ai])));
                g2.fillRoundRect(x,y,bw,bh,6,6);
                g2.setColor(new Color(AC[r.ai].getRed(),AC[r.ai].getGreen(),AC[r.ai].getBlue(),60));
                g2.setStroke(new BasicStroke(4)); g2.drawRoundRect(x-1,y-1,bw+2,bh+2,6,6);
                g2.setStroke(new BasicStroke(1));
                g2.setColor(TX); g2.setFont(FS); FontMetrics fm=g2.getFontMetrics();
                String v=r.ms+"ms"; g2.drawString(v,x+(bw-fm.stringWidth(v))/2,y-5);
                g2.setColor(AC[r.ai]); String n=ab(r.name);
                g2.drawString(n,x+(bw-fm.stringWidth(n))/2,top+ch+22);
            }
            axes(g2,pad,top,cw,ch);
        }
        void lineChart(Graphics2D g2){
            int pad=80,top=50,cw=getWidth()-pad-20,ch=getHeight()-top-60;
            long mx=results.stream().mapToLong(r->r.ms).max().orElse(1);
            grid(g2,pad,top,cw,ch,mx);
            int step=cw/(results.size()+1),px=-1,py=-1;
            for(int i=0;i<results.size();i++){
                BR r=results.get(i); int x=pad+step*(i+1),y=top+(int)(ch-(double)r.ms/mx*ch);
                if(px>=0){g2.setColor(new Color(100,180,255,120));g2.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g2.drawLine(px,py,x,y);g2.setStroke(new BasicStroke(1));}
                g2.setColor(AC[r.ai]); g2.fillOval(x-8,y-8,16,16);
                g2.setColor(BC); g2.fillOval(x-4,y-4,8,8);
                g2.setColor(TX); g2.setFont(FS); g2.drawString(r.ms+"ms",x-15,y-14);
                g2.setColor(AC[r.ai]); g2.drawString(ab(r.name),x-18,top+ch+22);
                px=x; py=y;
            }
            axes(g2,pad,top,cw,ch);
        }
        void hBar(Graphics2D g2){
            int pad=140,top=30,rg=8,cw=getWidth()-pad-30;
            int rh=(getHeight()-top-30-rg*results.size())/Math.max(1,results.size());
            long mx=results.stream().mapToLong(r->r.ms).max().orElse(1);
            for(int i=0;i<results.size();i++){
                BR r=results.get(i); int bw=(int)((double)r.ms/mx*cw),y=top+i*(rh+rg);
                g2.setColor(AC[r.ai]); g2.setFont(FS); g2.drawString(r.name,8,y+rh/2+5);
                g2.setPaint(new GradientPaint(pad,y,br(AC[r.ai]),pad+bw,y,dk(AC[r.ai])));
                g2.fillRoundRect(pad,y,bw,rh,5,5);
                g2.setColor(TX); g2.drawString(r.ms+" ms",pad+bw+8,y+rh/2+5);
            }
        }
        void grid(Graphics2D g2,int pad,int top,int w,int h,long mx){
            for(int i=0;i<=5;i++){int y=top+h-(h*i/5);
                g2.setColor(BO); g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{3},0));
                g2.drawLine(pad,y,pad+w,y); g2.setStroke(new BasicStroke(1));
                g2.setColor(TM); g2.setFont(FS); g2.drawString((mx*i/5)+"ms",5,y+4);}
        }
        void axes(Graphics2D g2,int pad,int top,int w,int h){
            g2.setColor(TM); g2.setStroke(new BasicStroke(2));
            g2.drawLine(pad,top,pad,top+h); g2.drawLine(pad,top+h,pad+w,top+h);
            g2.setStroke(new BasicStroke(1)); g2.setFont(new Font("Monospaced",Font.BOLD,12));
            g2.setColor(CY); g2.drawString("Time (ms)",5,top-5);
            g2.drawString("Algorithms →",pad+w/2-40,top+h+45);
        }
        Color br(Color c){return new Color(Math.min(255,c.getRed()+40),Math.min(255,c.getGreen()+40),Math.min(255,c.getBlue()+40));}
        Color dk(Color c){return new Color(c.getRed()/2,c.getGreen()/2,c.getBlue()/2);}
        String ab(String s){String[]p=s.split(" ");if(p.length==1)return s.substring(0,Math.min(5,s.length()));if(p[0].equals("Arrays.sort"))return "Tim";return p[0].substring(0,Math.min(3,p[0].length()))+"."+p[1].substring(0,Math.min(3,p[1].length()));}
    }

    void addLog(String m){SwingUtilities.invokeLater(()->{log.append(m+"\n");log.setCaretPosition(log.getDocument().getLength());});}
    void setSt(String m){SwingUtilities.invokeLater(()->status.setText("  "+m));}
    JLabel lbl(String t,Font f,Color c){JLabel l=new JLabel(t);l.setFont(f);l.setForeground(c);l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;}
    Box.Filler vs(int h){return (Box.Filler)Box.createVerticalStrut(h);}
    JPanel tp(){JPanel p=new JPanel();p.setOpaque(false);return p;}

    JButton btn(String t,Color fg,Color bg){
        JButton b=new JButton(t){protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed()?fg.darker().darker():getModel().isRollover()?new Color(fg.getRed(),fg.getGreen(),fg.getBlue(),40):bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
            g2.setColor(fg); g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
            super.paintComponent(g);}};
        b.setFont(FB); b.setForeground(fg); b.setBackground(bg);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    JComboBox<String> combo(String[]items){
        JComboBox<String> cb=new JComboBox<>(items); cb.setBackground(BC); cb.setForeground(TX);
        cb.setFont(new Font("Monospaced",Font.BOLD,13));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?>l,Object v,int i,boolean sel,boolean f){
            JLabel lb=(JLabel)super.getListCellRendererComponent(l,v,i,sel,f);
            lb.setBackground(sel?CY:BC); lb.setForeground(sel?BD:TX);
            lb.setFont(new Font("Monospaced",Font.BOLD,13));
            lb.setBorder(BorderFactory.createEmptyBorder(4,8,4,8)); return lb;}});
        return cb;
    }

    JCheckBox chk(String t,Color c){
        JCheckBox cb=new JCheckBox(t); cb.setBackground(BP); cb.setForeground(c);
        cb.setFont(FB); cb.setFocusPainted(false);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT); return cb;
    }

    JScrollPane dScroll(Component c){
        JScrollPane sp=new JScrollPane(c);
        sp.getViewport().setBackground(BC); sp.setBackground(BC);
        sp.setBorder(BorderFactory.createLineBorder(BO)); return sp;
    }

    void styleTable(JTable t){
        t.setBackground(BC); t.setForeground(TX); t.setFont(FS);
        t.setGridColor(BO); t.setRowHeight(26);
        t.setSelectionBackground(new Color(0,100,180)); t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(BD); t.getTableHeader().setForeground(CY);
        t.getTableHeader().setFont(FH); t.setShowGrid(true);
        t.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable tb,Object v,boolean s,boolean f,int r,int c){
                Component cp=super.getTableCellRendererComponent(tb,v,s,f,r,c);
                if(!s)cp.setBackground(r%2==0?BC:new Color(20,30,52));
                cp.setForeground(c==3?CY:c==6?GR:TX); return cp;}});
    }

    TitledBorder tBorder(String t){
        TitledBorder b=BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BO),"  "+t+"  ");
        b.setTitleFont(FH); b.setTitleColor(CY); return b;
    }

    static class BR{
        String name,dt; int size,ai; long ms,comps,swaps;
        BR(String n,int sz,String d,long m,long c,long s,int a){name=n;size=sz;dt=d;ms=m;comps=c;swaps=s;ai=a;}
    }

    public static void main(String[]a){
        try{UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());}catch(Exception e){}
        UIManager.put("ComboBox.background",BC);
        UIManager.put("ComboBox.foreground",TX);
        SwingUtilities.invokeLater(AlgorithmComparator::new);
    }
}