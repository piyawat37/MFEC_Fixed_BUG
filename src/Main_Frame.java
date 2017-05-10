import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;

/*********************/
/*JAVA Developer Test*/
/* Piyawat Pemwattana*/
/******* MFEC ********/
/*********************/

public class Main_Frame extends JPanel implements ActionListener {

	
	//Frame Layout
	private JFrame frame;
	static private final String newline = "\n";
	static private final String space = " | ";
	JButton btnOpenALog;
	JFileChooser fc;
	JLabel fileName;
	JTextArea logData;
	JScrollPane scroll;
	
	//Calculate
	static String log_Data[];
	
	//Story of time	
	//-- InTime
	static int inTime;
	static int hourInTime;
	static int minInTime;
	//-- OutTime
	static int outTime;
	static int hourOutTime;
	static int minOutTime;
	static int timeMin;
	
	//-- OT-Time;
	static int otTime;
	static double WageOT;
	
	
	//-- Time Processing
	static int timeWorkedOutIn; //Example 8:00 - 16:00
	static int timeWorkedInOut; //Example 16.00 - 8.00
	
	//-- Time Display
	static String timeResult;
	static String[] time;
	
	
	//-- Story of Money
	static double moneyHour = 36.25;
	static double moneyMin = 0.60416666666666666666666666666667;
	static DecimalFormat Decf = new DecimalFormat("##.00");
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main_Frame window = new Main_Frame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main_Frame() {
		fc = new JFileChooser();
		frame = new JFrame();
		frame.getContentPane().setForeground(UIManager.getColor("Button.background"));
		frame.setBounds(100, 100, 600, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		btnOpenALog = new JButton("Open a Log File . . .");
		btnOpenALog.setBounds(10, 11, 138, 34);
        btnOpenALog.addActionListener(this);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(btnOpenALog);
		fileName = new JLabel();
		fileName.setBounds(167, 11, 257, 34);
		frame.getContentPane().add(fileName);
	
		
		logData = new JTextArea(12,60);
		logData.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scroll = new JScrollPane(logData, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		logData.setBounds(10, 56, 414, 194);
		logData.setEditable(false);
		frame.getContentPane().setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(scroll);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setResizable(false);
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == btnOpenALog){
			int returnVal = fc.showOpenDialog(Main_Frame.this);
			
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                fileName.setText("File : " + file.getName() + "." + newline);
                try {
                	logData.setText("");
					ArrayList<String> log_list = new ArrayList<String>(Files.readAllLines(Paths.get(file.getPath())));
					log_Data = new String[log_list.size()];
					for(int i=0;i<log_Data.length;i++){ //GET ROW
						log_Data[i] = log_list.get(i);
						//System.out.println(log_Data[i]);
						String[] col = log_Data[i].split("\\|");
						logData.append(col[0]+space);
						System.out.println(col[0]);
						for(int j=0; j<col.length;j++){ //GET COLUMN TIME
							if(j==2){
								if(!col[j].isEmpty()){
									//logData.append("IN: "+col[j]+space);
									System.out.println("IN: "+col[j]);
									time = col[j].split(":");
									hourInTime = Integer.parseInt(time[0]);
									minInTime = Integer.parseInt(time[1]);
									inTime = (hourInTime*60)+minInTime;
								}else{
									logData.append("Invalid"+newline);
								}
							}
							if(j==4){
								if(!col[j].isEmpty()){
									//logData.append("OUT: "+col[j]+space);
									System.out.println("OUT: "+col[j]);
									time = col[j].split(":");
									hourOutTime = Integer.parseInt(time[0]);
									minOutTime = Integer.parseInt(time[1]);
									outTime = (hourOutTime*60)+minOutTime;
									System.out.println("In: "+inTime+" minute");
									System.out.println("Out: "+outTime+" minute");
									timeWorkedOutIn = outTime - inTime; 
									timeWorkedInOut = inTime - outTime;
									if(inTime<outTime){ //Morning - Evening
										timeResult = String.valueOf((timeWorkedOutIn)/60)+":"+String.valueOf((timeWorkedOutIn)%60);
										if(CheckHoliday(col[1])){ //Holiday
											if(hourInTime < 8 || hourInTime == 8 && minInTime < 06){
												if(outTime>1050){
													otTime = outTime - 1050; //1050 = 17.30 Start OT
													WageOT = (Double.parseDouble(CalculateWageOT(otTime))*2) + (Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))*1.5);
													logData.append("Wage: "+WageOT+" (OT) (Holiday) "+newline);
												}else{
													logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))*1.5+ " (Holiday) " +newline);
												}
											}else if(hourInTime > 17 && minInTime > 30){
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))*2+ " (Holiday) " +newline);												
											}else if(inTime>485){
												//Late Holiday
												if(outTime>1050){
													otTime = outTime - 1050; //1050 = 17.30 Start OT
													WageOT = (Double.parseDouble(CalculateWageOT(otTime))*2) + (Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))*1.5)
																-Double.parseDouble(Calculate_Late(inTime-485)) ;
													logData.append("Wage: "+WageOT+" (OT) (Holiday Late) "+newline);
												}else{
													logData.append("Wage: "+((Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))*1.5)-Double.parseDouble(Calculate_Late(inTime-485)))
															+ " (Holiday Late) " +newline);
												}
											}
										}else { //Worked Day
											if(hourInTime < 8 || hourInTime == 8 && minInTime < 06){
												if(outTime>1050){
													otTime = outTime - 1050; //1050 = 17.30 Start OT
													WageOT = (Double.parseDouble(CalculateWageOT(otTime))*1.5) + Double.parseDouble(CalculateWageNormal(timeWorkedOutIn));
													logData.append("Wage: "+WageOT+" (OT) "+newline);
												}else{
													logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))+newline);
												}
											}else if(hourInTime > 17 && minInTime > 30){
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))*1.5+newline);												
											}else if(inTime>485){
												//Late Worked Day
												if(outTime>1050){
													otTime = outTime - 1050; //1050 = 17.30 Start OT
													WageOT = (Double.parseDouble(CalculateWageOT(otTime))*1.5) + Double.parseDouble(CalculateWageNormal(timeWorkedOutIn))
																-Double.parseDouble(Calculate_Late(inTime-485)) ;
													logData.append("Wage: "+WageOT+" (OT) (Worked Day Late) "+newline);
												}else{
													logData.append("Wage: "+((Double.parseDouble(CalculateWageNormal(timeWorkedOutIn)))-Double.parseDouble(Calculate_Late(inTime-485)))
															+ " (Holiday Late) " +newline);
												}
											}
										}
									}else if(outTime<inTime){ //Evening - Morning
										timeResult = String.valueOf((timeWorkedInOut)/60)+":"+String.valueOf((timeWorkedInOut)%60);
										if(CheckHoliday(col[1])){ //Holiday
											if(hourInTime < 8 || hourInTime == 8 && minInTime < 06){
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedInOut))*1.5+newline);
											}else if(hourInTime > 8){
												//Late Holiday
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedInOut))*1.5+newline);
											}else if(hourInTime > 17 && minInTime > 30){
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedInOut))*2+newline);												
											}
										}else { //Worked Day
											if(hourInTime < 8 || hourInTime == 8 && minInTime < 06){
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedInOut))+newline);
											}else if(hourInTime > 8){
												//Late Worked Day
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedInOut))+newline);
											}else if(hourInTime > 17 && minInTime > 30){
												logData.append("Wage: "+Double.parseDouble(CalculateWageNormal(timeWorkedInOut))*1.5+newline);												
											}
										}
									}
									//logData.append("Time Working result: "+ timeResult+newline);
									System.out.println("Time Working result: "+ timeWorkedOutIn+newline);
								}else{
									logData.append("Invalid"+newline);
								}
							}
						}
						System.out.println("Length: "+col.length);
						System.out.println();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            } else {
                fileName.setText("");
            }
		}
	}
	/*
	public static boolean CheckHoliday(String date){
		String daySplit[];
		int dayOfWeek;
		String Value = null;
		Calendar c = Calendar.getInstance();
	    c.set(2015, Calendar.OCTOBER, 8);
	    DateFormat df = new SimpleDateFormat("EEE:d/MM/yyyy");
	    for (int i = 0; i < 4; i++) {
	    	daySplit = df.format(c.getTime()).split(":");
	    	if(date.equals(daySplit[1])){
	    		if(daySplit[0].equals("ส.") || daySplit[0].equals("อา.")){
		    		return true;
	    		}
	    		break;
	    	}
	    	c.add(Calendar.DATE,  1);
	    }
		return false;
	}*/
	public static boolean CheckHoliday(String date){
		SimpleDateFormat format = new SimpleDateFormat("d/M/yyyy");
		try{
			Date d = format.parse(date);
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			if(Calendar.SUNDAY == dayOfWeek || Calendar.SATURDAY == dayOfWeek){
				return true;
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return false;
	}
	public static String CalculateWageNormal(int timework){
		String wage = null;
		double calWage;
		calWage = timework * moneyMin;
		if(calWage > 290){
			calWage = 290.00;
		}else if(calWage > 435){
			calWage = 435.00;
		}
		wage = Decf.format(calWage);
		return wage;
	}
	public static String CalculateWageOT(int timeworkOT){
		String wage = null;
		double calWage;
		calWage = timeworkOT * moneyMin;
		wage = Decf.format(calWage);
		return wage;
	}
	public static String Calculate_Late(int timelate){
		String diswage = null;
		double calDisWage;
		calDisWage = timelate*moneyMin;
		diswage = Decf.format(calDisWage);
		return diswage;
	}
}
