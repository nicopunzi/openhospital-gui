package org.isf.visits.gui;

/**
 * @author Mwithi
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;

import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.GenericReportPatient;
import org.isf.stat.gui.report.WardVisitsReport;
import org.isf.therapy.manager.TherapyManager;
import org.isf.therapy.model.Therapy;
import org.isf.therapy.model.TherapyRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.JAgenda;
import org.isf.utils.jobjects.DayCalendar;
import org.isf.utils.jobjects.JAgenda.AgendaDayObject;
import org.isf.visits.gui.InsertVisit;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class VisitView extends JDialog {

	private JAgenda jAgenda;

	private JPanel northPanel;
	private JPanel monthYearPanel;
	private JYearChooser yearChooser;
	private JMonthChooser monthChooser;
	private JPanel patientPanel;
	private JPanel eastPanel;
	private JPanel southPanel;
	private JTextArea noteTextArea;
	private JScrollPane noteScrollPane;
	private JPanel therapyPanel;
	private JPanel visitPanel;
	private JPanel notifyAndSMSPanel;
	private JPanel actionsPanel;
	private Patient patient;
	private boolean admitted;
	private JButton addVisit;
	private JButton removeVisit;

	private JButton removeTherapy;
	private JButton addTherapy;
	private JButton editTherapy;
	private JButton checkTherapy;
	private JLabel therapyCheckLabel;
	private JButton checkIconButton;
	private JButton smsIconButton;
	private JButton notifyIconButton;
	private JCheckBox notifyCheckBox;
	private JCheckBox smsCheckBox;
	private JButton closeButton;
	private JButton saveButton;
	// private JButton reportButton; TODO to enable when a report will be designed

	private boolean checked = false;
	private boolean available = false;
	private boolean therapyModified = false;
	private boolean notifiable = false;
	private boolean smsenable = false;
	private boolean visitModified = false;
	private Therapy selectedTherapy;
	private Visit selectedVisit;
	private Hashtable<Integer, Therapy> hashTableTherapy;
	private Hashtable<Integer, TherapyRow> hashTableThRow;
	private Hashtable<Integer, Visit> hashTableVisits;

	private static final int TherapyButtonWidth = 200;
	private static final int VisitButtonWidth = 200;
	private static final int ActionsButtonWidth = 240;
	private static final int AllButtonHeight = 30;

	private static final long serialVersionUID = 1L;

	private MedicalBrowsingManager medBrowser = new MedicalBrowsingManager();
	private TherapyManager thManager = Context.getApplicationContext().getBean(TherapyManager.class);
	private VisitManager vstManager = Context.getApplicationContext().getBean(VisitManager.class);
	private ArrayList<Medical> medArray;
	private ArrayList<Double> qtyArray = new ArrayList<Double>();
	private ArrayList<Therapy> therapies = new ArrayList<Therapy>();
	private ArrayList<TherapyRow> thRows = new ArrayList<TherapyRow>();
	private ArrayList<Visit> visits = new ArrayList<Visit>();
	private Ward ward;
	private boolean ad;
	private JPanel wardPanel;
	private JButton todayButton;
	private JButton tomorrowButton;

	private JTable jTableFirst;

	private JScrollPane jScrollPaneFirstday;

	public VisitView() {
		initComponents();
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				// force close operation
				closeButton.doClick();

				// to free memory
				if (medArray != null)
					medArray.clear();
				if (therapies != null)
					therapies.clear();
				if (thRows != null)
					thRows.clear();
				if (qtyArray != null)
					qtyArray.clear();
				if (visits != null)
					visits.clear();
			}
		});
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setResizable(false);
		setTitle("Visits");
		final int x = (screenSize.width - getWidth()) / 2;
		final int y = (screenSize.height - getHeight()) / 2;
		setLocation(x, y);
		setVisible(true);
		
	}

	private void initComponents() {

		getContentPane().setLayout(new BorderLayout());
		try {
			visits = vstManager.getVisitsWard();
		} catch (OHServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * HashTable of the visits
		 */
		hashTableVisits = new Hashtable<Integer, Visit>();
		if (!visits.isEmpty()) {
			for (Visit visit : visits) {
				hashTableVisits.put(visit.getVisitID(), visit);
			}
		}
		getContentPane().add(getNorthPanel(), BorderLayout.NORTH);
		getContentPane().add(dayCalendar(), BorderLayout.CENTER);
		setSize(850, 570);
	}

	private JPanel dayCalendar() {
		sl_visitParamsPanel = new SpringLayout();
		JPanel visitParamPanel = new JPanel(sl_visitParamsPanel);

		GridBagLayout gbl_jPanelData = new GridBagLayout();
		gbl_jPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 00 };
		gbl_jPanelData.rowHeights = new int[] { 20, 20, 20, 0, 0, 0, 0, 0 };
		gbl_jPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_jPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		visitParamPanel.setLayout(gbl_jPanelData);

		GridBagConstraints gbc_buttonback = new GridBagConstraints();
		gbc_buttonback.fill = GridBagConstraints.VERTICAL;
		gbc_buttonback.anchor = GridBagConstraints.WEST;

		gbc_buttonback.gridy = 0;
		gbc_buttonback.gridx = 0;

		visitParamPanel.add(getButtonBack(), gbc_buttonback);
		
		GridBagConstraints gbc_datelabel = new GridBagConstraints();
		gbc_datelabel.fill = GridBagConstraints.VERTICAL;
		gbc_datelabel.anchor = GridBagConstraints.WEST;

		gbc_datelabel.gridy = 0;
		gbc_datelabel.gridx = 1;

		visitParamPanel.add(getDateFirstDay(), gbc_datelabel);

		GridBagConstraints gbc_Duration = new GridBagConstraints();
		gbc_Duration.fill = GridBagConstraints.VERTICAL;
		gbc_Duration.anchor = GridBagConstraints.WEST;
		gbc_Duration.gridy = 1;
		gbc_Duration.gridx = 1;
		visitParamPanel.add(getVisitFirstday(), gbc_Duration);

		GridBagConstraints gbc_datesecond = new GridBagConstraints();
		gbc_datesecond.fill = GridBagConstraints.VERTICAL;
		gbc_datesecond.anchor = GridBagConstraints.WEST;

		gbc_datesecond.gridy = 0;
		gbc_datesecond.gridx = 2;

		visitParamPanel.add(getDatesecondDay(), gbc_datesecond);
		
		
		GridBagConstraints gbc_buttonnext= new GridBagConstraints();
		gbc_buttonnext.fill = GridBagConstraints.VERTICAL;
		gbc_buttonnext.anchor = GridBagConstraints.WEST;

		gbc_buttonnext.gridy = 0;
		gbc_buttonnext.gridx = 3;

		visitParamPanel.add(getButtonNext(), gbc_buttonnext);

		GridBagConstraints gbc_date = new GridBagConstraints();
		gbc_date.fill = GridBagConstraints.VERTICAL;
		gbc_date.anchor = GridBagConstraints.WEST;

		gbc_date.gridy = 1;
		gbc_date.gridx = 2;

		visitParamPanel.add(getVisitSecondDay(), gbc_date);
		
		GridBagConstraints gbc_printfirst = new GridBagConstraints();
		gbc_printfirst.fill = GridBagConstraints.CENTER;
		gbc_printfirst.anchor = GridBagConstraints.CENTER;

		gbc_printfirst.gridy = 2;
		gbc_printfirst.gridx = 1;

		visitParamPanel.add(getPrintTodayButton(), gbc_printfirst);
		
		GridBagConstraints gbc_printsecond = new GridBagConstraints();
		gbc_printsecond.fill = GridBagConstraints.CENTER;
		gbc_printsecond.anchor = GridBagConstraints.CENTER;

		gbc_printsecond.gridy = 2;
		gbc_printsecond.gridx = 2;

		visitParamPanel.add(gettomorrowTodayButton(), gbc_printsecond);

		
		GridBagConstraints gbc_close = new GridBagConstraints();
		gbc_close.fill = GridBagConstraints.WEST;
		gbc_printfirst.anchor = GridBagConstraints.WEST;

		gbc_close.gridy = 3;
		gbc_close.gridx = 3;

		visitParamPanel.add(getCloseButton(), gbc_close);


		return visitParamPanel;

	}

	private JPanel datefirstPanel;

	private JLabel dateFirstLabel;

	private JPanel getDateFirstDay() {

		if (datefirstPanel == null) {

			datefirstPanel = new JPanel();

			dateFirstLabel = new JLabel();
	
				dateFirstLabel.setText(getDate());
			

			// otherDiaAdmField.setText(manager.getAdmissionVisit().getGa());
			datefirstPanel.add(dateFirstLabel);

		}
		return datefirstPanel;
	}

	private JPanel datesecondPanel;

	private JLabel datesecondLabel;

	private JPanel getDatesecondDay() {
		if (datesecondPanel == null) {

			datesecondPanel = new JPanel();

			datesecondLabel = new JLabel();
		
				datesecondLabel.setText(getDateDayAfter(new Date()));
			
			// otherDiaAdmField.setText(manager.getAdmissionVisit().getGa());
			datesecondPanel.add(datesecondLabel);

		}
		return datesecondPanel;
	}

	private JScrollPane getVisitFirstday() {
		if (jScrollPaneFirstday == null) {
			jScrollPaneFirstday = new JScrollPane();
			jScrollPaneFirstday.setViewportView(visitFirstDayPanel());
			jScrollPaneFirstday.setAlignmentY(Box.TOP_ALIGNMENT);
		
			jScrollPaneFirstday.setMinimumSize(new Dimension(300,400));
		}
		return jScrollPaneFirstday;
	}

	private int[] visColumsWidth = { 500, 350 };
	private boolean[] visColumsResizable = { false, true };
	private ArrayList<Visit> visitfirst = new ArrayList<Visit>();
	private ArrayList<Visit> visfirst = new ArrayList<Visit>();

	private JScrollPane jScrollPaneSecondtday;

	private JTable visitFirstDayPanel() {
		if (jTableFirst == null) {
			jTableFirst = new JTable();
			String dat = getDate();
			visfirst = getvisit((visits), dat);
			jTableFirst.setModel(new VisitModel());
			jTableFirst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTableFirst.setAutoCreateColumnsFromModel(false);
			jTableFirst.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

			ListSelectionModel listSelectionModel = jTableFirst.getSelectionModel();

		}
		return jTableFirst;
	}

	private JScrollPane getVisitSecondDay() {
		if (jScrollPaneSecondtday == null) {
			jScrollPaneSecondtday = new JScrollPane();
			sl_visitParamsPanel.putConstraint(SpringLayout.NORTH, jScrollPaneSecondtday, 0, SpringLayout.NORTH,
					getVisitFirstday());
			sl_visitParamsPanel.putConstraint(SpringLayout.EAST, jScrollPaneSecondtday, -104, SpringLayout.WEST,
					getVisitFirstday());
			jScrollPaneSecondtday.setViewportView(visitSecondDayPanel());
			jScrollPaneSecondtday.setAlignmentY(Box.TOP_ALIGNMENT);
			jScrollPaneSecondtday.setMinimumSize(new Dimension(300,400));
		}
		return jScrollPaneSecondtday;
	}

	private ArrayList<Visit> visSecond = new ArrayList<Visit>();
	private JTable jTableSecond;

	private JTable visitSecondDayPanel() {
		if (jTableSecond == null) {
			jTableSecond = new JTable();
			String dat = null;
			dat = getDateDayAfter(new Date());
			visSecond = getvisit((visits), dat);
			jTableSecond.setModel(new VisitSecondModel());
			jTableSecond.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			for (int i = 0; i < visColums.length; i++) {
				jTableFirst.getColumnModel().getColumn(i).setMinWidth(visColumsWidth[i]);
				if (!visColumsResizable[i])
					jTableFirst.getColumnModel().getColumn(i).setMaxWidth(visColumsWidth[i]);
			}
			jTableSecond.setAutoCreateColumnsFromModel(false);
			jTableSecond.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

			ListSelectionModel listSelectionModel = jTableSecond.getSelectionModel();

		}
		return jTableSecond;
	}

	private String getDateDayAfter(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		date = c.getTime();
		String dat = dateFormat.format(date);
		return dat;
	}
	
	private String getDateDayBefore(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -1);
		date = c.getTime();
		String dat = dateFormat.format(date);
		return dat;
	}

	private ArrayList<Visit> getvisit(ArrayList<Visit> arrayList, String dat) {
		ArrayList<Visit> vis = new ArrayList<Visit>();
		for (int i = 0; i < visits.size(); i++) {
			Visit visit = visits.get(i);
			GregorianCalendar d = visits.get(i).getDate();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); // lowercase "dd"
			String da = formatter.format(d.getTime());
			if (da.equals(dat)&&(visits.get(i).getWard().equals(wardBox.getSelectedItem()))) {
				vis.add(visits.get(i));
			}
		}
		return vis;
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(LEFT);
			return cell;
		}
	}

	public String[] visColums = { MessageBundle.getMessage("Visits"),

	};
	private SpringLayout sl_visitParamsPanel;

	private JButton backButton;

	private JButton nextButton;

	class VisitModel extends DefaultTableModel {
		public VisitModel() {
		}

		public int getRowCount() {
			if (visfirst == null)
				return 0;
			return visfirst.size();
		}

		public String getColumnName(int c) {
			return visColums[c];
		}

		public int getColumnCount() {
			return visColums.length;
		}

		public Object getValueAt(int r, int c) {

			Visit visit = visfirst.get(r);
			GregorianCalendar d = visfirst.get(r).getDate();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); 
			String da = formatter.format(d.getTime());
			String dat = getDate();
			return visit.getPatient().getName() + ", " + visit.getPatient().getCode() + ", Service: " + visit.getService();

		}
	}

	class VisitSecondModel extends DefaultTableModel {
		public VisitSecondModel() {
		}

		public int getRowCount() {
			if (visSecond == null)
				return 0;
			return visSecond.size();
		}

		public String getColumnName(int c) {
			return visColums[c];
		}

		public int getColumnCount() {
			return visColums.length;
		}

		public Object getValueAt(int r, int c) {

			Visit visit = visSecond.get(r);
			GregorianCalendar d = visSecond.get(r).getDate();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); // lowercase "dd"
			String da = formatter.format(d.getTime());
			String dat = getDate();
			
			return visit.getPatient().getName() + ", " + visit.getPatient().getCode() + ", Service: " + visit.getService();

		}
	}

	private String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private void showTherapies() {

		hashTableTherapy = new Hashtable<Integer, Therapy>();
		for (Therapy th : therapies) {
			hashTableTherapy.put(th.getTherapyID(), th);
			showTherapy(th);
		}
	}

	private void showTherapy(Therapy th) {
		for (GregorianCalendar gc : th.getDates()) {
			if (gc.get(GregorianCalendar.YEAR) == yearChooser.getYear()) {
				if (gc.get(GregorianCalendar.MONTH) == monthChooser.getMonth()) {
					jAgenda.addElement(th, gc.get(GregorianCalendar.DAY_OF_MONTH));
					notifyCheckBox.setSelected(th.isNotify());
				}
			}
		}
	}


	private JButton getCheckIconButton() {
		if (checkIconButton == null) {
			checkIconButton = new JButton();
			checkIconButton.setIcon(new ImageIcon("rsc/icons/delete_dialog.png"));
			checkIconButton.setOpaque(false);
			checkIconButton.setBorderPainted(false);
			checkIconButton.setFocusPainted(false);
			checkIconButton.setContentAreaFilled(false);
			checkIconButton.setMaximumSize(new Dimension(Short.MAX_VALUE, AllButtonHeight));
			checkIconButton.setMinimumSize(new Dimension(AllButtonHeight, AllButtonHeight));
		}
		return checkIconButton;
	}

	private JButton getPrintTodayButton() {
		if (todayButton == null) {
			todayButton = new JButton();
			todayButton.setMnemonic(KeyEvent.VK_R);
			todayButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			todayButton.setText(MessageBundle.getMessage("angal.visit.visittoday")); //$NON-NLS-1$
			todayButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String da= dateFirstLabel.getText();
					DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					Date date = null;
					
						try {
							date = format.parse(da);
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						 ward= (Ward) wardBox.getSelectedItem();
					new WardVisitsReport(ward.getCode(), date, GeneralData.VISITSHEET);
					dispose();
				}
			});
		}
		return todayButton;
	}

	private JButton gettomorrowTodayButton() {
		if (tomorrowButton == null) {
			tomorrowButton = new JButton();
			tomorrowButton.setMnemonic(KeyEvent.VK_R);
			tomorrowButton.setText(MessageBundle.getMessage("angal.visit.visittoday")); //$NON-NLS-1$
			tomorrowButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					String da= datesecondLabel.getText();
					DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					Date date = null;
					
						try {
							date = format.parse(da);
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						 ward= (Ward) wardBox.getSelectedItem();
					new WardVisitsReport(ward.getCode(), date, GeneralData.VISITSHEET);
					dispose();
				}
			});
		}
		return tomorrowButton;
	}



	private JButton getButtonNext() {
		if (nextButton == null) {
			nextButton = new JButton("Next->"); //$NON-NLS-1$
			nextButton.setMnemonic(KeyEvent.VK_X);
			nextButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			nextButton.setHorizontalAlignment(SwingConstants.LEFT);
			nextButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String da= datesecondLabel.getText();
					DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					Date date = null;
					try {
						date = format.parse(da);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String dat=getDateDayAfter(date);
					visfirst = getvisit((visits), da);
					dateFirstLabel.setText(da);
					datesecondLabel.setText(dat);

					visSecond = getvisit((visits), dat);
				    ((VisitModel) jTableFirst.getModel()).fireTableDataChanged();
				    jTableFirst.updateUI();
				    ((VisitSecondModel) jTableSecond.getModel()).fireTableDataChanged();
				    jTableSecond.updateUI();
				}
			});
		}
		return nextButton;
	}
	
	private JButton getButtonBack() {
		if (backButton == null) {
			backButton = new JButton("<-back"); //$NON-NLS-1$
			backButton.setMnemonic(KeyEvent.VK_X);
			backButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			backButton.setHorizontalAlignment(SwingConstants.LEFT);
			backButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String da= dateFirstLabel.getText();
					DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					Date date = null;
					try {
						date = format.parse(da);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String dat=getDateDayBefore(date);
					visSecond = getvisit((visits), da);
					dateFirstLabel.setText(dat);
					datesecondLabel.setText(da);
					ad=true;
					visfirst.clear();
					visfirst = getvisit((visits), dat);
				    ((VisitModel) jTableFirst.getModel()).fireTableDataChanged();
				    jTableFirst.updateUI();
				    ((VisitSecondModel) jTableSecond.getModel()).fireTableDataChanged();
				    jTableSecond.updateUI();
				}
			});
		}
		return backButton;
	}
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close")); //$NON-NLS-1$
			closeButton.setIcon(new ImageIcon("rsc/icons/close_button.png"));
			closeButton.setMnemonic(KeyEvent.VK_X);
			closeButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			closeButton.setHorizontalAlignment(SwingConstants.LEFT);
			closeButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// to free memory
					if (therapyModified || visitModified) {
						int ok = JOptionPane.showConfirmDialog(VisitView.this,
								MessageBundle.getMessage("angal.common.save") + "?"); //$NON-NLS-1$
						if (ok == JOptionPane.YES_OPTION) {
							saveButton.doClick();
						} else if (ok == JOptionPane.NO_OPTION) {
							// NO -> do nothing
						} else if (ok == JOptionPane.CANCEL_OPTION) {
							return;
						}
					}

					if (medArray != null)
						medArray.clear();
					if (therapies != null)
						therapies.clear();
					if (thRows != null)
						thRows.clear();
					if (qtyArray != null)
						qtyArray.clear();
					if (visits != null)
						visits.clear();
					dispose();
				}
			});
		}
		return closeButton;
	}



	protected void updateCheckLabel() {
		if (checked) {
			if (available) {
				checkIconButton.setIcon(new ImageIcon("rsc/icons/ok_dialog.png"));
				therapyCheckLabel.setText(MessageBundle.getMessage("angal.therapy.availablem")); //$NON-NLS-1$
				therapyCheckLabel.setForeground(Color.GREEN);
			} else {
				checkIconButton.setIcon(new ImageIcon("rsc/icons/delete_dialog.png"));
				therapyCheckLabel.setText(MessageBundle.getMessage("angal.therapy.notavailablem")); //$NON-NLS-1$
				therapyCheckLabel.setForeground(Color.RED);
			}
		} else {
			checkIconButton.setIcon(new ImageIcon("rsc/icons/delete_dialog.png"));
			therapyCheckLabel.setText(MessageBundle.getMessage("angal.therapy.notcheckedm")); //$NON-NLS-1$
			therapyCheckLabel.setForeground(Color.RED);
		}

	}


	private JPanel getNorthPanel() {
		if (northPanel == null) {
			northPanel = new JPanel(new GridLayout(0, 2));

			if (ad) {
				northPanel.add(getPatientPanel());
			} else {
				northPanel.add(getWardPanel());
			}

		}
		return northPanel;
	}

	private JPanel getPatientPanel() {
		if (patientPanel == null) {
			patientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			String patientString = MessageBundle.getMessage("angal.therapy.therapyfor") + " " + patient.getName();
			JLabel patientLabel = new JLabel(patientString);
			patientLabel.setFont(new Font("Serif", Font.PLAIN, 30));
			patientPanel.add(patientLabel);

		}
		return patientPanel;
	}
	private JComboBox wardBox;
	private Ward saveWard = null;
	private ArrayList<Ward> wardList = null;
	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();
			
			WardBrowserManager wbm = new WardBrowserManager();
			wardBox = new JComboBox();
			wardBox.addItem("");
			try {
				wardList = wbm.getWards();
			}catch(OHServiceException e){
				wardList = new ArrayList<Ward>();
                OHServiceExceptionUtil.showMessages(e);
			}
			for (Ward ward : wardList) {
		
					if (ward.getBeds() > 0)
						wardBox.addItem(ward);
				
				if (saveWard != null) {
					if (saveWard.getCode().equalsIgnoreCase(ward.getCode())) {
						wardBox.setSelectedItem(ward);
					}
				} 
				}
			
			wardBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					
					String da= dateFirstLabel.getText();
					DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					Date date = null;
					try {
						date = format.parse(da);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String dat=getDateDayAfter(date);
					visfirst = getvisit((visits), da);
					dateFirstLabel.setText(da);
					datesecondLabel.setText(dat);

					visSecond = getvisit((visits), dat);
				    ((VisitModel) jTableFirst.getModel()).fireTableDataChanged();
				    jTableFirst.updateUI();
				    ((VisitSecondModel) jTableSecond.getModel()).fireTableDataChanged();
				    jTableSecond.updateUI();
				}
			});
			
		
			}

			
			wardPanel.add(wardBox);
			wardPanel.setBorder(BorderFactory.createTitledBorder("Select a ward:"));
		
		return wardPanel;
	}
	
	public Ward getWard() {
	
		return  (Ward) wardBox.getSelectedItem();
		
	}

}
