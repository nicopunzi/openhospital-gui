package org.isf.medicalstockward.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.management.StringValueExp;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.gui.MovStockMultipleCharging;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstockward.gui.WardPharmacyNew.StockMovModel;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.RequestFocusListener;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.time.TimeTools;
import org.isf.ward.model.Ward;

public class WardPharmacyRectify extends JDialog {

	//LISTENER INTERFACE --------------------------------------------------------
    private EventListenerList movementWardListeners = new EventListenerList();
	
	public interface MovementWardListeners extends EventListener {
		public void movementInserted(AWTEvent e);
	}
	
	public void addMovementWardListener(MovementWardListeners l) {
		movementWardListeners.add(MovementWardListeners.class, l);
	}
	
	public void removeMovementWardListener(MovementWardListeners listener) {
		movementWardListeners.remove(MovementWardListeners.class, listener);
	}
	
	private void fireMovementWardInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;};
		
		EventListener[] listeners = movementWardListeners.getListeners(MovementWardListeners.class);
		for (int i = 0; i < listeners.length; i++)
			((MovementWardListeners)listeners[i]).movementInserted(event);
	}
	//---------------------------------------------------------------------------
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	
	private JTextField jTextFieldReason;
	private Ward wardSelected;
	private JComboBox jComboBoxMedical;
	private JLabel jLabelStockQty;
	private JSpinner jSpinnerNewQty;

	private boolean lotExist;
	
	//Medicals (ALL)
	private MedicalBrowsingManager medManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	
	private ArrayList<Medical> medicals;
	private HashMap<String, Medical> medicalMap; //map medicals by their prod_code
	private HashMap<Integer, Double> wardMap; //map quantities by their medical_id
	private JTextField jTextFieldLotn;
	private JButton jButtonChooseLot;
	private ArrayList<MedicalWard> wardDrugs;
	private static final String DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy"; //$NON-NLS-1$
	private MovWardBrowserManager wardManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private Medical medical;
	private JButton jButtonNewLot;
	private boolean newLot;
	
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			WardPharmacyRectify dialog = new WardPharmacyRectify();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WardPharmacyRectify() {
		initMedicals();
		initComponents();
	}
	
	private void initMedicals() {
		try {
			this.medicals = medManager.getMedicals();
		} catch (OHServiceException e) {
			this.medicals = null;
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	/**
	 * Create the dialog.
	 */
	public WardPharmacyRectify(JFrame owner, Ward ward) {
		super(owner, true);
		wardSelected = ward;
		try {
			wardDrugs= wardManager.getMedicalsWard(wardSelected.getCode().charAt(0), false);
		} catch (OHServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wardMap = new HashMap<Integer, Double>();
		ArrayList<Double> list;
		for (MedicalWard medWard : wardDrugs) {
			
			if(wardMap.containsKey(medWard.getMedical().getCode())){
			 Double qu = wardMap.get(medWard.getMedical().getCode());
			 wardMap.put(medWard.getMedical().getCode(), qu + medWard.getQty());
			} else {
				wardMap.put(medWard.getMedical().getCode(), medWard.getQty());
			}
			
		}
		medicalMap = new HashMap<String, Medical>();
		if (null != medicals) {
			for (Medical med : medicals) {
				medicalMap.put(med.getProd_code(), med);
			}
		}
		
		initMedicals();
		initComponents();
	}
	public WardPharmacyRectify(JFrame owner, Ward ward, Medical medical) {
		super(owner, true);
		wardSelected = ward;
		medical= medical;
		try {
			wardDrugs= wardManager.getMedicalsWard(wardSelected.getCode().charAt(0), false);
		} catch (OHServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wardMap = new HashMap<Integer, Double>();
		ArrayList<Double> list;
		for (MedicalWard medWard : wardDrugs) {
			
			if(wardMap.containsKey(medWard.getMedical().getCode())){
			 Double qu = wardMap.get(medWard.getMedical().getCode());
			 wardMap.put(medWard.getMedical().getCode(), qu + medWard.getQty());
			} else {
				wardMap.put(medWard.getMedical().getCode(), medWard.getQty());
			}
			
		}
		medicalMap = new HashMap<String, Medical>();
		if (null != medicals) {
			for (Medical med : medicals) {
				medicalMap.put(med.getProd_code(), med);
			}
		}
		
		initMedicals();
		initComponents();
		jComboBoxMedical.setSelectedItem(medical);
		
	}
	
	private void initComponents() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel jLabelRectifyTitle = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.title")); //$NON-NLS-1$
			jLabelRectifyTitle.setForeground(Color.RED);
			jLabelRectifyTitle.setFont(new Font("Tahoma", Font.PLAIN, 28)); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelRectifyTitle = new GridBagConstraints();
			gbc_jLabelRectifyTitle.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelRectifyTitle.gridx = 1;
			gbc_jLabelRectifyTitle.gridy = 0;
			contentPanel.add(jLabelRectifyTitle, gbc_jLabelRectifyTitle);
		}
		{
			JLabel jLabelStock = new JLabel(MessageBundle.getMessage("angal.medicalstockwardrectify.instock")); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelStock = new GridBagConstraints();
			gbc_jLabelStock.anchor = GridBagConstraints.SOUTH;
			gbc_jLabelStock.insets = new Insets(0, 0, 5, 0);
			gbc_jLabelStock.gridx = 4;
			gbc_jLabelStock.gridy = 1;
			contentPanel.add(jLabelStock, gbc_jLabelStock);
		}
		{
			JLabel jLabelMedical = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.medical")); //$NON-NLS-1$
			jLabelMedical.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelMedical.setPreferredSize(new Dimension(100, 25));
			GridBagConstraints gbc_jLabelMedical = new GridBagConstraints();
			gbc_jLabelMedical.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelMedical.anchor = GridBagConstraints.EAST;
			gbc_jLabelMedical.gridx = 0;
			gbc_jLabelMedical.gridy = 2;
			contentPanel.add(jLabelMedical, gbc_jLabelMedical);
		}
		{
			GridBagConstraints gbc_jComboBoxMedical = new GridBagConstraints();
			gbc_jComboBoxMedical.insets = new Insets(0, 0, 5, 5);
			gbc_jComboBoxMedical.fill = GridBagConstraints.HORIZONTAL;
			gbc_jComboBoxMedical.gridx = 1;
			gbc_jComboBoxMedical.gridy = 2;
			gbc_jComboBoxMedical.gridwidth = 2;
			contentPanel.add(getJComboBoxMedical(), gbc_jComboBoxMedical);
		}
		{
			GridBagConstraints gbc_jPanelLot = new GridBagConstraints();
			gbc_jPanelLot.insets = new Insets(0, 0, 5, 5);
			gbc_jPanelLot.fill = GridBagConstraints.HORIZONTAL;
			gbc_jPanelLot.gridx = 0;
			gbc_jPanelLot.gridy = 3;
			gbc_jPanelLot.gridwidth = 3;
			contentPanel.add(getJPanelLot(), gbc_jPanelLot);
		}
		
		{
			GridBagConstraints gbc_jLabelStockQty = new GridBagConstraints();
			gbc_jLabelStockQty.insets = new Insets(0, 0, 5, 0);
			gbc_jLabelStockQty.gridx = 4;
			gbc_jLabelStockQty.gridy = 2;
			contentPanel.add(getJLabelStockQty(), gbc_jLabelStockQty);
		}
		{
			JLabel jLabelNewQuantity = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.actualquantity")); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelNewQuantity = new GridBagConstraints();
			gbc_jLabelNewQuantity.anchor = GridBagConstraints.EAST;
			gbc_jLabelNewQuantity.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelNewQuantity.gridx = 0;
			gbc_jLabelNewQuantity.gridy = 4;
			contentPanel.add(jLabelNewQuantity, gbc_jLabelNewQuantity);
		}
		{
			GridBagConstraints gbc_jSpinnerNewQuantity = new GridBagConstraints();
			gbc_jSpinnerNewQuantity.fill = GridBagConstraints.HORIZONTAL;
			gbc_jSpinnerNewQuantity.insets = new Insets(0, 0, 5, 5);
			gbc_jSpinnerNewQuantity.gridx = 1;
			gbc_jSpinnerNewQuantity.gridy = 4;
			gbc_jSpinnerNewQuantity.gridwidth = 2;
			contentPanel.add(getJSpinnerNewQty(), gbc_jSpinnerNewQuantity);
		}
		{
			JLabel jLabelReason = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.reason")); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelReason = new GridBagConstraints();
			gbc_jLabelReason.anchor = GridBagConstraints.EAST;
			gbc_jLabelReason.insets = new Insets(0, 0, 0, 5);
			gbc_jLabelReason.gridx = 0;
			gbc_jLabelReason.gridy = 5;
			contentPanel.add(jLabelReason, gbc_jLabelReason);
		}
		{
			jTextFieldReason = new JTextField();
			GridBagConstraints gbc_jTextFieldReason = new GridBagConstraints();
			gbc_jTextFieldReason.insets = new Insets(0, 0, 0, 5);
			gbc_jTextFieldReason.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldReason.gridx = 1;
			gbc_jTextFieldReason.gridy = 5;
			gbc_jTextFieldReason.gridwidth = 2;
			contentPanel.add(jTextFieldReason, gbc_jTextFieldReason);
			
		}
		{
			JPanel jButtonPanel = new JPanel();
			jButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(jButtonPanel, BorderLayout.SOUTH);
			{
				JButton jButtonOk = new JButton(MessageBundle.getMessage("angal.common.ok")); //$NON-NLS-1$
				jButtonOk.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Object item;
						Medical med;
						
						/*
						 *  To override MovWardBrowserManager.validateMovementWard() behaviour
						 */
						try {
							item = jComboBoxMedical.getSelectedItem();
						if (item instanceof Medical) {
						
							med = (Medical) jComboBoxMedical.getSelectedItem();
						}else {
							JOptionPane.showMessageDialog(WardPharmacyRectify.this, MessageBundle.getMessage("angal.medicalstockward.rectify.pleaseselectadrug")); //$NON-NLS-1$
							return;
						}
						} catch (ClassCastException e1) {
							JOptionPane.showMessageDialog(WardPharmacyRectify.this, MessageBundle.getMessage("angal.medicalstockward.rectify.pleaseselectadrug")); //$NON-NLS-1$
							return;
						}
						
						/*
						 *  To override MovWardBrowserManager.validateMovementWard() behaviour
						 */
						String reason = jTextFieldReason.getText().trim();
						if (reason.equals("")) { //$NON-NLS-1$
							JOptionPane.showMessageDialog(WardPharmacyRectify.this, MessageBundle.getMessage("angal.medicalstockward.rectify.pleasespecifythereason")); //$NON-NLS-1$
							return;
						}
					
				
						
						Double stock = Double.parseDouble(jLabelStockQty.getText());
						Double newQty = (Double) jSpinnerNewQty.getValue();
						double quantity = stock.doubleValue() - newQty.doubleValue();
						double x = -1 * newQty.doubleValue() ;
						if ((stock.doubleValue() == 0.0 && newQty.doubleValue() < 0.0)||(x>stock.doubleValue())) {
							
							
							StringBuilder message = new StringBuilder();
							message.append(MessageBundle.getMessage("angal.medicalstockward.rectifyerror"));
							JOptionPane.showMessageDialog(WardPharmacyRectify.this, message.toString());
							return ;
						
						}
						if (quantity == 0.) return;
						
						boolean result;
						try {
							Lot lot = new Lot() ;
							String lotCode = jTextFieldLotn.getText();
							if (lotCode.equals("")) { //$NON-NLS-1$
								JOptionPane.showMessageDialog(WardPharmacyRectify.this, MessageBundle.getMessage("angal.medicalstock.pleaseselectalot")); //$NON-NLS-1$
								return;
							}
							lot.setCode(lotCode);
							
							result = movWardBrowserManager.newMovementWard(new MovementWard(
									wardSelected, 
									new GregorianCalendar(), 
									false, null, 0, 0, 
									reason, 
									med, 
									quantity,
									MessageBundle.getMessage("angal.medicalstockward.rectify.pieces"),
									lot));
							if (result) {
								fireMovementWardInserted();
								dispose();
							} else return;
							
						} catch (OHServiceException e1) {
							result = false;
							OHServiceExceptionUtil.showMessages(e1);
						}
						
					}
				});
				jButtonPanel.add(jButtonOk);
			}
			{
				JButton jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel")); //$NON-NLS-1$
				jButtonCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				jButtonPanel.add(jButtonCancel);
			}
		}
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJPanelLot() {
		

		// patientPanel.setPreferredSize(new Dimension());
		JPanel lotPanel = new JPanel(new SpringLayout());

		GridBagLayout gbl_jPanelData = new GridBagLayout();
		gbl_jPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 00 };
		
		gbl_jPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_jPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		lotPanel.setLayout(gbl_jPanelData);
		
		{
			JLabel jLabelLot = new JLabel(MessageBundle.getMessage("angal.medicalstockward.lotnumberabb")); //$NON-NLS-1$
			jLabelLot.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelLot.setPreferredSize(new Dimension(100, 25));
			GridBagConstraints gbc_jLabelMedical = new GridBagConstraints();
			gbc_jLabelMedical.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelMedical.anchor = GridBagConstraints.EAST;
			gbc_jLabelMedical.gridx = 0;
			gbc_jLabelMedical.gridy = 0;
			lotPanel.add(jLabelLot, gbc_jLabelMedical);
		}
	
		{
			jTextFieldLotn = new JTextField();
			jTextFieldLotn.setEditable(false);
			GridBagConstraints gbc_jTextFieldReason = new GridBagConstraints();
			gbc_jTextFieldReason.insets = new Insets(0, 0, 0, 5);
			gbc_jTextFieldReason.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldReason.gridx = 1;
			gbc_jTextFieldReason.gridy = 0;
			lotPanel.add(jTextFieldLotn, gbc_jTextFieldReason);
			jTextFieldLotn.setColumns(10);
		}
		{
		
			GridBagConstraints gbc_jbuttonLot = new GridBagConstraints();
			gbc_jbuttonLot.insets = new Insets(0, 0, 0, 5);
			gbc_jbuttonLot.fill = GridBagConstraints.HORIZONTAL;
			gbc_jbuttonLot.gridx = 2;
			gbc_jbuttonLot.gridy = 0;
			lotPanel.add(getJButtonChooseLot(), gbc_jbuttonLot);
		
		}
		{
			
			GridBagConstraints gbc_jbuttonNewLot = new GridBagConstraints();
			gbc_jbuttonNewLot.insets = new Insets(0, 0, 0, 5);
			gbc_jbuttonNewLot.fill = GridBagConstraints.HORIZONTAL;
			gbc_jbuttonNewLot.gridx = 3;
			gbc_jbuttonNewLot.gridy = 0;
			lotPanel.add(getJButtonNewLot(), gbc_jbuttonNewLot);
		
		}
		return lotPanel;
	}

	private JButton getJButtonNewLot() {
		if (jButtonNewLot == null) {
			jButtonNewLot = new JButton();
			jButtonNewLot.setText(MessageBundle.getMessage("angal.medicalstockward.newlot")); //$NON-NLS-1$

			jButtonNewLot.addActionListener(new ActionListener() {

				

				public void actionPerformed(ActionEvent e) {
					Medical med = (Medical) jComboBoxMedical.getSelectedItem();
					String medical=med.getDescription();
					newLot=true;
					chooseLot(wardDrugs ,medical);
					 
				}
			});
		}
		return jButtonNewLot;
	}
	private JButton getJButtonChooseLot() {
		if (jButtonChooseLot == null) {
			jButtonChooseLot = new JButton();
			jButtonChooseLot.setText(MessageBundle.getMessage("angal.medicalstockward.chooselot")); //$NON-NLS-1$

			jButtonChooseLot.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					Medical med = (Medical) jComboBoxMedical.getSelectedItem();
					String medical=med.getDescription();
					newLot=false;
					 chooseLot(wardDrugs ,medical);
				}
			});
		}
		return jButtonChooseLot;
	}


	private MedicalWard chooseLot(ArrayList<MedicalWard> drug, String me) {
		MedicalWard medWard =null;
		
		ArrayList<MedicalWard> dr = new ArrayList<MedicalWard>();
		
		for (MedicalWard elem : drug) {
			if(elem.getMedical().getDescription().equals(me)) {
				
					MedicalWard e = elem;
						dr.add(e);
					
				
			}
		}
		if(newLot) {
			Medical med = (Medical) jComboBoxMedical.getSelectedItem();
			int qty = askQuantity(med);
			if (qty == 0) {return null;};
			Lot addLot = askLot();
			if (addLot == null) {
				return null;
			}
			
			
			
			jTextFieldLotn.setText( addLot.getCode());
			jSpinnerNewQty.setValue(Double.valueOf(qty));
			jLabelStockQty.setText("0");
			BigDecimal cost = askCost(qty);
			addLot.setCost(cost);
			try {
				boolean res = movStockInsertingManager.storeLot(addLot.getCode(), addLot, med);
			} catch (OHServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			
		JTable lotTable = new JTable(new StockMovModel(dr));
		lotTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.selectalot")), BorderLayout.NORTH); //$NON-NLS-1$
		panel.add(new JScrollPane(lotTable), BorderLayout.CENTER);
		
		do {
			int ok = JOptionPane.showConfirmDialog(WardPharmacyRectify.this, 
					panel, 
					MessageBundle.getMessage("angal.medicalstockward.rectify.lotinformations"), //$NON-NLS-1$ 
					JOptionPane.OK_CANCEL_OPTION);

			if (ok == JOptionPane.OK_OPTION) {
				int row = lotTable.getSelectedRow();
				if (row != -1) medWard = dr.get(row);
					else return null;
				
				
				
				jTextFieldLotn.setText( medWard.getLot().getCode());
				jSpinnerNewQty.setValue(medWard.getQty());
				jLabelStockQty.setText(medWard.getQty().toString());
				
			}
		} while (dr == null);
		}
		return medWard;
	}
	private boolean isAutomaticLot() {
		return GeneralData.AUTOMATICLOT_IN;
	}

	protected Lot askLot() {
		GregorianCalendar preparationDate = new GregorianCalendar();
		GregorianCalendar expiringDate = new GregorianCalendar();
		Lot lot = null;
		JTextField lotNameTextField = new JTextField(15);
		lotNameTextField.addAncestorListener(new RequestFocusListener());
		if (isAutomaticLot())
			lotNameTextField.setEnabled(false);
		CustomJDateChooser preparationDateChooser = new CustomJDateChooser(new Date());
		{
			preparationDateChooser.setDateFormatString(DATE_FORMAT_DD_MM_YYYY);
		}
		CustomJDateChooser expireDateChooser = new CustomJDateChooser(new Date());
		{
			expireDateChooser.setDateFormatString(DATE_FORMAT_DD_MM_YYYY);
		}
		JPanel panel = new JPanel(new GridLayout(3, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.lotnumberabb"))); //$NON-NLS-1$
		panel.add(lotNameTextField);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.preparationdate"))); //$NON-NLS-1$
		panel.add(preparationDateChooser);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.expiringdate"))); //$NON-NLS-1$
		panel.add(expireDateChooser);

		do {
			int ok = JOptionPane.showConfirmDialog(WardPharmacyRectify.this, panel,
					MessageBundle.getMessage("angal.medicalstockward.rectify.lotinformations"), //$NON-NLS-1$
					JOptionPane.OK_CANCEL_OPTION);

			if (ok == JOptionPane.OK_OPTION) {
				String lotName = lotNameTextField.getText();
				if (lotName.isEmpty()) {
					JOptionPane.showMessageDialog(WardPharmacyRectify.this,
							MessageBundle.getMessage("angal.medicalstockward.rectify.lotnumberSelect")); //$NON-NLS-1$
					return null;
				}
				if (expireDateChooser.getDate().before(preparationDateChooser.getDate())) {
					JOptionPane.showMessageDialog(WardPharmacyRectify.this,
							MessageBundle.getMessage("angal.medicalstockward.rectify.expirydatebeforepreparationdate"));

				} else if (expireDateChooser.getDate().before(preparationDateChooser.getDate())) {
					JOptionPane.showMessageDialog(WardPharmacyRectify.this,
							MessageBundle.getMessage("angal.medicalstockward.rectify.expirydatebeforepreparationdate"));

				} else {
					expiringDate.setTime(expireDateChooser.getDate());
					preparationDate.setTime(preparationDateChooser.getDate());
					lot = new Lot(lotName, preparationDate, expiringDate);
				}
			} else {
				return null;
			}
		} while (lot == null);
		return lot;

	}
	protected BigDecimal askCost(int qty) {
		double cost = 0.;
		do {
			String input = JOptionPane.showInputDialog(WardPharmacyRectify.this, 
					MessageBundle.getMessage("angal.medicalstockward.rectify.unitcost"),  //$NON-NLS-1$
					0.);
			if (input != null) {
				try {
					cost = Double.parseDouble(input);
					if (cost < 0)
						throw new NumberFormatException();
					else if (cost == 0.) {
						double total = askTotalCost();
						//if (total == 0.) return;
						cost = total / qty;
					}
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(WardPharmacyRectify.this, 
							MessageBundle.getMessage("angal.medicalstockward.rectify.pleaseinsertavalidvalue")); //$NON-NLS-1$
				}
			} else return new BigDecimal(cost);
		} while (cost == 0.);
		return new BigDecimal(cost);
	}
	
	protected double askTotalCost() {
		String input = JOptionPane.showInputDialog(WardPharmacyRectify.this, 
				MessageBundle.getMessage("angal.medicalstockward.rectify.totalcost"), //$NON-NLS-1$
				0.);
		double total = 0.;
		if (input != null) {
			try {
				total = Double.parseDouble(input);
				if (total < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(WardPharmacyRectify.this, 
						MessageBundle.getMessage("angal.medicalstockward.rectify.pleaseinsertavalidvalue")); //$NON-NLS-1$
			}
		}
		return total;
	}
	protected int askQuantity(Medical med) {
		StringBuilder title = new StringBuilder(MessageBundle.getMessage("angal.common.quantity")); //$NON-NLS-1$
		StringBuilder message = new StringBuilder(med.toString());
		String prodCode = med.getProd_code();
		if (prodCode != null && !prodCode.equals("")) {
			title.append(" ").append(MessageBundle.getMessage("angal.common.code")); //$NON-NLS-1$ //$NON-NLS-2$
			title.append(": ").append(prodCode); //$NON-NLS-1$
		} else { 
			title.append(": "); //$NON-NLS-1$
		}
		int qty = 0;
		do {
			String quantity = JOptionPane.showInputDialog(WardPharmacyRectify.this, 
					message.toString(), 
					title.toString(),
					JOptionPane.QUESTION_MESSAGE);
			if (quantity != null) {
				try {
					qty = Integer.parseInt(quantity);
					if (qty == 0)
						return 0;
					if (qty < 0)
						throw new NumberFormatException();
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(WardPharmacyRectify.this, 
							MessageBundle.getMessage("angal.medicalstockward.invalidquantitypleasetryagain")); //$NON-NLS-1$
					qty = 0;
				}
			} else return qty;
		} while (qty == 0);
		return qty;
	}
	
	
	/**
	 * @return
	 */
	private JSpinner getJSpinnerNewQty() {
		if (jSpinnerNewQty == null) {
			jSpinnerNewQty = new JSpinner(new SpinnerNumberModel(0.0, null, null, 1));
			jSpinnerNewQty.setFont(new Font("Tahoma", Font.BOLD, 14)); //$NON-NLS-1$
			jSpinnerNewQty.addChangeListener(new ChangeListener() {
			      

				@Override
				public void stateChanged(ChangeEvent e) {
					Medical med = ((Medical) jComboBoxMedical.getSelectedItem());
					Double stock = Double.parseDouble(jLabelStockQty.getText());
					Double newQty = (Double) jSpinnerNewQty.getValue();
					if (newQty<0 && stock ==0.0) {
						jButtonChooseLot.setEnabled(false);
						jButtonNewLot.setEnabled(false);
					}else if (newQty>0 && !lotExist) {
						jButtonChooseLot.setEnabled(false);
						jButtonNewLot.setEnabled(true);
					} else if (newQty<0 && -(newQty)>stock) {
						jButtonChooseLot.setEnabled(false);
						jButtonNewLot.setEnabled(false);
					} else if (newQty<0){
						jButtonChooseLot.setEnabled(true);
						jButtonNewLot.setEnabled(false);
					} else if (newQty>0) {
						jButtonChooseLot.setEnabled(true);
						jButtonNewLot.setEnabled(true);
					}
					
				}
			});
			
	
			

		}
		return jSpinnerNewQty;
	}

	/**
	 * @return
	 */
	private JLabel getJLabelStockQty() {
		if (jLabelStockQty == null) {
			jLabelStockQty = new JLabel(""); //$NON-NLS-1$
			jLabelStockQty.setHorizontalAlignment(SwingConstants.CENTER);
			jLabelStockQty.setPreferredSize(new Dimension(100, 25));
			jLabelStockQty.setFont(new Font("Tahoma", Font.BOLD, 14)); //$NON-NLS-1$
		}
		return jLabelStockQty;
	}

	/**
	 * @return
	 */
	private JComboBox getJComboBoxMedical() {
		if (jComboBoxMedical == null) {
			jComboBoxMedical = new JComboBox();
			jComboBoxMedical.addItem(""); //$NON-NLS-1$
			for (Medical med : medicals){
				jComboBoxMedical.addItem(med);
			}
		
			jComboBoxMedical.addActionListener(new ActionListener() {
				

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Medical med = ((Medical) jComboBoxMedical.getSelectedItem());
						ArrayList<MedicalWard> war = wardDrugs;
						jButtonChooseLot.setEnabled(false);
						jButtonNewLot.setEnabled(true);
						lotExist=false;
						for (MedicalWard medWard : wardDrugs) {
							if (med.getDescription().equals(medWard.getMedical().getDescription())) {
								jButtonChooseLot.setEnabled(true);
								jButtonNewLot.setEnabled(false);
								lotExist=true;
							}
						}
						Integer code = med.getCode();
						Double qty = wardMap.get(code);
						if (qty == null) qty = new Double(0);
						jLabelStockQty.setText(qty.toString());
						jSpinnerNewQty.setValue(qty);
					} catch (ClassCastException ex) {
						jLabelStockQty.setText(""); //$NON-NLS-1$
						jSpinnerNewQty.setValue(new Double(0));
					}
				}
			});
		}
		return jComboBoxMedical;
	}
	private MovStockInsertingManager movManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	
	class StockMovModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private ArrayList<MedicalWard> druglist;

		public StockMovModel(ArrayList<MedicalWard> drug) {
			druglist = drug;
		}

		public int getRowCount() {
			if (druglist == null)
				return 0;
			return druglist.size();
		}

		public String getColumnName(int c) {
			if (c == 0) {
				return MessageBundle.getMessage("angal.medicalstock.lotid"); //$NON-NLS-1$
			}
			
			if (c == 1) {
				return MessageBundle.getMessage("angal.medicalstock.duedate"); //$NON-NLS-1$
			}
			if (c == 2) {
				return MessageBundle.getMessage("angal.common.quantity"); //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}

		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int r, int c) {
			if (c == -1) {
				return druglist.get(r);
			} else if (c == 0) {
				return druglist.get(r).getId().getLot().getCode();
			} else if (c == 1) {
				ArrayList<Lot> lot = null;
				try {
					lot = movManager.getLotByMedical(druglist.get(r).getId().getMedical());
				} catch (OHServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return TimeTools.formatDateTime(lot.get(0).getDueDate(), DATE_FORMAT_DD_MM_YYYY);
			}  else if (c == 2) {
				return druglist.get(r).getQty();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
