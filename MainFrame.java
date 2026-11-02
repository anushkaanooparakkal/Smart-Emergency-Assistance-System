package com.seas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MainFrame extends JFrame {
    private final EmergencyService service = new EmergencyService();
    private final DefaultTableModel emergencyModel = model("ID", "Location", "Incident", "Severity", "Status", "Ambulance", "Hospital", "Created");
    private final DefaultTableModel hospitalModel = model("ID", "Hospital", "Address", "Phone", "Beds", "ICU Beds", "ER Ready");
    private final DefaultTableModel ambulanceModel = model("ID", "Driver", "Phone", "Vehicle", "Type", "Status", "Base Location");
    private final Color navy = new Color(24, 43, 91);
    private final Color blue = new Color(47, 105, 201);

    public MainFrame() {
        setTitle("Smart Emergency Assistance System (SEAS)");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(1180, 740); setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null); setLayout(new BorderLayout());
        JLabel heading = new JLabel("  SMART EMERGENCY ASSISTANCE SYSTEM");
        heading.setOpaque(true); heading.setBackground(navy); heading.setForeground(Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22)); heading.setBorder(new EmptyBorder(22, 18, 22, 18)); add(heading, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboard()); tabs.addTab("Report Emergency", report()); tabs.addTab("Registration", registration());
        tabs.addTab("Ambulances", ambulances()); tabs.addTab("Hospitals", hospitals()); tabs.addTab("Vitals", vitals()); tabs.addTab("Status Tracking", status());
        add(tabs, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout()); bottom.setBorder(new EmptyBorder(6, 12, 6, 12));
        bottom.add(new JLabel("Academic demonstration only - not connected to real emergency services."), BorderLayout.WEST);
        JButton refresh = new JButton("Refresh Data"); refresh.addActionListener(e -> refreshAll()); bottom.add(refresh, BorderLayout.EAST); add(bottom, BorderLayout.SOUTH);
        refreshAll();
    }

    private JPanel dashboard() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBorder(new EmptyBorder(18, 18, 18, 18));
        p.add(new JLabel("<html><h2>Emergency Operations Dashboard</h2>Create a report, assign a demo ambulance, select a hospital, record vitals and update status.</html>"), BorderLayout.NORTH);
        p.add(new JScrollPane(table(emergencyModel)), BorderLayout.CENTER); return p;
    }
    private JPanel report() {
        JPanel p = formPanel(); JTextField user = field(), location = field(), type = field(), description = field();
        JComboBox<String> severity = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});
        row(p,"User ID (optional)",user); row(p,"Incident location",location); row(p,"Incident type",type); row(p,"Severity",severity); row(p,"Description",description);
        JButton save = primary("Submit Emergency"); save.addActionListener(e -> run(() -> {
            if(location.getText().isBlank()||type.getText().isBlank()) throw new IllegalArgumentException("Location and incident type are required.");
            Long uid = user.getText().isBlank()?null:Long.valueOf(user.getText().trim());
            long id=service.reportEmergency(uid,location.getText(),type.getText(),(String)severity.getSelectedItem(),description.getText());
            info("Emergency recorded. ID: "+id+"\nStatus: PENDING"); location.setText("");type.setText("");description.setText("");user.setText("");refreshAll();
        })); row(p,"",save); return wrap(p,"Create a new emergency report");
    }
    private JPanel registration() {
        JPanel p=formPanel(); JTextField name=field(),email=field(),phone=field(),blood=field(),contact=field(); JPasswordField password=new JPasswordField();
        row(p,"Full name",name);row(p,"Email",email);row(p,"Password",password);row(p,"Phone",phone);row(p,"Blood group (optional)",blood);row(p,"Emergency contact",contact);
        JButton save=primary("Register User"); save.addActionListener(e -> run(() -> {
            if(name.getText().isBlank()||email.getText().isBlank()||password.getPassword().length==0) throw new IllegalArgumentException("Name, email and password are required.");
            long id=service.registerUser(name.getText(),email.getText(),new String(password.getPassword()),phone.getText(),blood.getText(),contact.getText());
            info("User registered. User ID: "+id);name.setText("");email.setText("");password.setText("");phone.setText("");blood.setText("");contact.setText("");
        }));row(p,"",save);return wrap(p,"User / citizen registration");
    }
    private JPanel ambulances() {
        JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,12,12,12));p.add(new JScrollPane(table(ambulanceModel)),BorderLayout.CENTER);
        JPanel f=new JPanel(new GridLayout(2,5,6,6));JTextField driver=field(),phone=field(),vehicle=field(),location=field();JComboBox<String> type=new JComboBox<>(new String[]{"BLS","ALS","BASIC","ADVANCED"});
        f.add(labelled("Driver",driver));f.add(labelled("Phone",phone));f.add(labelled("Vehicle number",vehicle));f.add(labelled("Type",type));f.add(labelled("Base location",location));
        JButton add=primary("Add Ambulance");add.addActionListener(e->run(()->{if(driver.getText().isBlank()||vehicle.getText().isBlank())throw new IllegalArgumentException("Driver and vehicle number are required.");service.addAmbulance(driver.getText(),phone.getText(),vehicle.getText(),(String)type.getSelectedItem(),location.getText());info("Ambulance added.");refreshAll();}));
        JPanel south=new JPanel(new BorderLayout());south.add(f,BorderLayout.CENTER);south.add(add,BorderLayout.EAST);p.add(south,BorderLayout.SOUTH);return p;
    }
    private JPanel hospitals() {
        JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,12,12,12));p.add(new JScrollPane(table(hospitalModel)),BorderLayout.CENTER);
        JPanel f=new JPanel(new GridLayout(2,5,6,6));JTextField name=field(),address=field(),phone=field(),beds=field(),icu=field();
        f.add(labelled("Hospital name",name));f.add(labelled("Address",address));f.add(labelled("Phone",phone));f.add(labelled("Available beds",beds));f.add(labelled("ICU beds",icu));
        JButton add=primary("Add Hospital");add.addActionListener(e->run(()->{if(name.getText().isBlank()||address.getText().isBlank())throw new IllegalArgumentException("Hospital name and address are required.");int b=beds.getText().isBlank()?0:Integer.parseInt(beds.getText());int i=icu.getText().isBlank()?0:Integer.parseInt(icu.getText());if(b<0||i<0||i>b)throw new IllegalArgumentException("Beds must be non-negative and ICU beds cannot exceed total beds.");service.addHospital(name.getText(),address.getText(),phone.getText(),b,i);info("Hospital added.");refreshAll();}));
        JPanel south=new JPanel(new BorderLayout());south.add(f,BorderLayout.CENTER);south.add(add,BorderLayout.EAST);p.add(south,BorderLayout.SOUTH);return p;
    }
    private JPanel vitals() {
        JPanel p=formPanel();JTextField emergency=field(),pulse=field(),bp=field(),spo2=field(),temp=field(),notes=field();
        row(p,"Emergency ID",emergency);row(p,"Pulse rate (bpm)",pulse);row(p,"Blood pressure",bp);row(p,"SpO2 (%)",spo2);row(p,"Temperature C",temp);row(p,"Notes",notes);
        JButton save=primary("Save Vitals");save.addActionListener(e->run(()->{long id=Long.parseLong(emergency.getText().trim());Integer pr=pulse.getText().isBlank()?null:Integer.valueOf(pulse.getText().trim());Integer oxygen=spo2.getText().isBlank()?null:Integer.valueOf(spo2.getText().trim());Double tc=temp.getText().isBlank()?null:Double.valueOf(temp.getText().trim());if(pr!=null&&(pr<0||pr>300))throw new IllegalArgumentException("Pulse must be between 0 and 300.");if(oxygen!=null&&(oxygen<0||oxygen>100))throw new IllegalArgumentException("SpO2 must be between 0 and 100.");service.addVitals(id,pr,bp.getText(),oxygen,tc,notes.getText());info("Vitals saved for emergency #"+id+".");}));row(p,"",save);return wrap(p,"Record patient vital signs");
    }
    private JPanel status() {
        JPanel p=formPanel();JTextField emergency=field(),hospital=field();JComboBox<String> status=new JComboBox<>(new String[]{"PENDING","ASSIGNED","IN_TRANSIT","ADMITTED","COMPLETED","CANCELLED"});
        row(p,"Emergency ID",emergency);row(p,"Hospital ID (for dispatch)",hospital);
        JButton dispatch=primary("Assign Available Ambulance");dispatch.addActionListener(e->run(()->info(service.dispatchAmbulance(Long.parseLong(emergency.getText().trim()),Long.parseLong(hospital.getText().trim())))));row(p,"",dispatch);
        row(p,"New status",status);JButton update=primary("Update Status");update.addActionListener(e->run(()->{service.updateStatus(Long.parseLong(emergency.getText().trim()),(String)status.getSelectedItem());info("Status updated.");refreshAll();}));row(p,"",update);
        row(p,"Note",new JLabel("Dispatch selects the first AVAILABLE ambulance. This demo does not use GPS routing."));return wrap(p,"Assign an ambulance and track emergency progress");
    }
    private void refreshAll(){load(emergencyModel,service::getEmergencies);load(hospitalModel,service::getHospitals);load(ambulanceModel,service::getAmbulances);}
    @FunctionalInterface private interface Action{void run()throws Exception;}
    @FunctionalInterface private interface Provider{List<Object[]> get()throws SQLException;}
    private void run(Action a){try{a.run();}catch(Exception ex){error(ex.getMessage()==null?ex.toString():ex.getMessage());}}
    private void load(DefaultTableModel m,Provider p){m.setRowCount(0);try{for(Object[] r:p.get())m.addRow(r);}catch(SQLException ex){m.addRow(new Object[]{"Database error: "+ex.getMessage()});}}
    private JPanel wrap(JPanel form,String title){JPanel p=new JPanel(new BorderLayout());p.setBorder(new EmptyBorder(22,26,22,26));JLabel h=new JLabel(title);h.setFont(new Font("SansSerif",Font.BOLD,18));h.setBorder(new EmptyBorder(0,0,16,0));p.add(h,BorderLayout.NORTH);p.add(form,BorderLayout.CENTER);return p;}
    private JPanel formPanel(){JPanel p=new JPanel(new GridBagLayout());p.setBorder(new EmptyBorder(5,5,5,5));return p;}
    private void row(JPanel p,String text,Component c){int y=p.getComponentCount()/2;GridBagConstraints l=new GridBagConstraints();l.gridx=0;l.gridy=y;l.insets=new Insets(7,5,7,12);l.anchor=GridBagConstraints.LINE_END;p.add(new JLabel(text),l);GridBagConstraints r=new GridBagConstraints();r.gridx=1;r.gridy=y;r.weightx=1;r.insets=new Insets(7,5,7,5);r.fill=GridBagConstraints.HORIZONTAL;if(c instanceof JComponent jc)jc.setPreferredSize(new Dimension(360,30));p.add(c,r);}
    private JPanel labelled(String text,JComponent c){JPanel p=new JPanel(new BorderLayout(3,3));p.add(new JLabel(text),BorderLayout.NORTH);p.add(c,BorderLayout.CENTER);return p;}
    private JTable table(DefaultTableModel m){JTable t=new JTable(m);t.setRowHeight(25);t.setAutoCreateRowSorter(true);t.getTableHeader().setBackground(navy);t.getTableHeader().setForeground(Color.WHITE);t.setFillsViewportHeight(true);return t;}
    private JButton primary(String s){JButton b=new JButton(s);b.setBackground(blue);b.setForeground(Color.WHITE);b.setFocusPainted(false);return b;}
    private JTextField field(){return new JTextField();}
    private void info(String s){JOptionPane.showMessageDialog(this,s,"SEAS",JOptionPane.INFORMATION_MESSAGE);}
    private void error(String s){JOptionPane.showMessageDialog(this,s,"SEAS Error",JOptionPane.ERROR_MESSAGE);}
    private static DefaultTableModel model(String... columns){return new DefaultTableModel(columns,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
}
