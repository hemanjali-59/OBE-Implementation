import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JOptionPane;

class DB {
	public static Connection conn = null;
	public static PreparedStatement pstmt = null;

	public static void connect(String path) {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CourseObjective_Create(int id, String code, String objCode, int no, String detail) {
		try {
			String q = "INSERT INTO courseobjective(ID, cour_id, cour_obj_code, cour_obj_no, cour_obj_detail) VALUES(?, ?, ?, ?, ?)";
			pstmt = conn.prepareStatement(q);
			pstmt.setInt(1, id);
			pstmt.setString(2, code);
			pstmt.setString(3, objCode);
			pstmt.setInt(4, no);
			pstmt.setString(5, detail);
			pstmt.executeUpdate();
			JOptionPane.showMessageDialog(null, "Record Inserted.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Insertion Failed.");
		}
	}
}

class CourseApp extends Frame implements ActionListener {
	Button createBtn = new Button("Create");
	Button updateBtn = new Button("Update");
	Button retrieveBtn = new Button("Retrieve");
	Button deleteBtn = new Button("Delete");

	Panel optionPanel = new Panel();
	Panel actionPanel = new Panel();

	CourseApp() {
		setTitle("Course Objective Manager");
		setSize(800, 700);
		setLayout(new BorderLayout());

		optionPanel.setLayout(new GridLayout(1, 4));
		optionPanel.add(createBtn);
		optionPanel.add(updateBtn);
		optionPanel.add(retrieveBtn);
		optionPanel.add(deleteBtn);

		add(optionPanel, BorderLayout.NORTH);
		add(actionPanel, BorderLayout.CENTER);

		createBtn.addActionListener(this);
		updateBtn.addActionListener(this);
		retrieveBtn.addActionListener(this);
		deleteBtn.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		DB.connect("C:\\Users\\lenovo\\OneDrive\\Desktop\\Source Code\\courseobjective.db");
		setVisible(true);
		CourseObjective_Retrieve(); // Load retrieve view by default
		actionPanel.revalidate();
		actionPanel.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		actionPanel.removeAll();
		if (e.getSource() == createBtn)
			CourseObjective_CreateForm();
		else if (e.getSource() == updateBtn)
			CourseObjective_Update();
		else if (e.getSource() == retrieveBtn)
			CourseObjective_Retrieve();
		else if (e.getSource() == deleteBtn)
			CourseObjective_Delete();
		actionPanel.revalidate();
		actionPanel.repaint();
	}

	void CourseObjective_CreateForm() {
		actionPanel.setLayout(new GridLayout(6, 2));
		TextField id = new TextField();
		TextField tf1 = new TextField();
		TextField tf2 = new TextField();
		TextField tf3 = new TextField();
		TextField tf4 = new TextField();
		Button save = new Button("Save");

		save.addActionListener(ae -> {
			try {
				DB.CourseObjective_Create(
						Integer.parseInt(id.getText()),
						tf1.getText(),
						tf2.getText(),
						Integer.parseInt(tf3.getText()),
						tf4.getText());
				id.setText("");
				tf1.setText("");
				tf2.setText("");
				tf3.setText("");
				tf4.setText("");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Invalid Input.");
			}
		});

		actionPanel.add(new Label("ID:"));
		actionPanel.add(id);
		actionPanel.add(new Label("Course ID:"));
		actionPanel.add(tf1);
		actionPanel.add(new Label("Objective Code:"));
		actionPanel.add(tf2);
		actionPanel.add(new Label("Objective No:"));
		actionPanel.add(tf3);
		actionPanel.add(new Label("Objective Detail:"));
		actionPanel.add(tf4);
		actionPanel.add(new Label(""));
		actionPanel.add(save);
	}

	void CourseObjective_Update() {
		actionPanel.setLayout(new BorderLayout());

		Panel formPanel = new Panel(new GridLayout(10, 1));
		TextArea allData = new TextArea(15, 80);
		allData.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(allData);

		TextField idField = new TextField();
		TextField tf1 = new TextField();
		TextField tf2 = new TextField();
		TextField tf3 = new TextField();
		TextField tf4 = new TextField();
		Button load = new Button("Load");
		Button update = new Button("Update");

		try {
			Statement stmt = DB.conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM courseobjective");
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-4s %-10s %-15s %-10s %-50s\n", "ID", "cour_id", "obj_code", "obj_no",
					"obj_detail"));
			sb.append("----------------------------------------------------------------------------------------\n");
			while (rs.next()) {
				sb.append(String.format(
						"%-4d %-10s %-15s %-10d %-50s\n",
						rs.getInt("ID"),
						rs.getString("cour_id"),
						rs.getString("cour_obj_code"),
						rs.getInt("cour_obj_no"),
						rs.getString("cour_obj_detail")));
			}
			allData.setText(sb.toString());
		} catch (Exception ex) {
		}

		load.addActionListener(ae -> {
			try {
				int id = Integer.parseInt(idField.getText());
				PreparedStatement ps = DB.conn.prepareStatement("SELECT * FROM courseobjective WHERE ID = ?");
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					tf1.setText(rs.getString("cour_id"));
					tf2.setText(rs.getString("cour_obj_code"));
					tf3.setText(String.valueOf(rs.getInt("cour_obj_no")));
					tf4.setText(rs.getString("cour_obj_detail"));
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Load Failed.");
			}
		});

		update.addActionListener(ae -> {
			try {
				int id = Integer.parseInt(idField.getText());
				String q = "UPDATE courseobjective SET cour_id=?, cour_obj_code=?, cour_obj_no=?, cour_obj_detail=? WHERE ID=?";
				PreparedStatement ps = DB.conn.prepareStatement(q);
				ps.setString(1, tf1.getText());
				ps.setString(2, tf2.getText());
				ps.setInt(3, Integer.parseInt(tf3.getText()));
				ps.setString(4, tf4.getText());
				ps.setInt(5, id);
				ps.executeUpdate();
				JOptionPane.showMessageDialog(null, "Record Updated.");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Update Failed.");
			}
		});

		formPanel.add(new Label("Enter ID to Update:"));
		formPanel.add(idField);
		formPanel.add(load);
		formPanel.add(new Label("Course ID:"));
		formPanel.add(tf1);
		formPanel.add(new Label("Objective Code:"));
		formPanel.add(tf2);
		formPanel.add(new Label("Objective No:"));
		formPanel.add(tf3);
		formPanel.add(new Label("Objective Detail:"));
		formPanel.add(tf4);
		formPanel.add(update);

		actionPanel.add(scrollPane, BorderLayout.CENTER);
		actionPanel.add(formPanel, BorderLayout.SOUTH);
	}

	void CourseObjective_Retrieve() {
		actionPanel.setLayout(new BorderLayout());
		TextArea output = new TextArea();
		output.setFont(new Font("Monospaced", Font.PLAIN, 12));
		try {
			Statement stmt = DB.conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM courseobjective");
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-4s %-10s %-15s %-10s %-50s\n", "ID", "cour_id", "obj_code", "obj_no",
					"obj_detail"));
			sb.append("----------------------------------------------------------------------------------------\n");
			while (rs.next()) {
				sb.append(String.format(
						"%-4d %-10s %-15s %-10d %-50s\n",
						rs.getInt("ID"),
						rs.getString("cour_id"),
						rs.getString("cour_obj_code"),
						rs.getInt("cour_obj_no"),
						rs.getString("cour_obj_detail")));
			}
			output.setText(sb.toString());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Retrieve Failed.");
		}
		actionPanel.add(output, BorderLayout.CENTER);
	}

	void CourseObjective_Delete() {
		actionPanel.setLayout(new BorderLayout());

		TextArea dataArea = new TextArea(15, 80);
		dataArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ScrollPane scroll = new ScrollPane();
		scroll.add(dataArea);

		Panel form = new Panel(new GridLayout(2, 2));
		TextField idField = new TextField();
		Button delete = new Button("Delete");

		try {
			Statement stmt = DB.conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM courseobjective");
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-4s %-10s %-15s %-10s %-50s\n", "ID", "cour_id", "obj_code", "obj_no",
					"obj_detail"));
			sb.append("----------------------------------------------------------------------------------------\n");
			while (rs.next()) {
				sb.append(String.format(
						"%-4d %-10s %-15s %-10d %-50s\n",
						rs.getInt("ID"),
						rs.getString("cour_id"),
						rs.getString("cour_obj_code"),
						rs.getInt("cour_obj_no"),
						rs.getString("cour_obj_detail")));
			}
			dataArea.setText(sb.toString());
		} catch (Exception ex) {
		}

		delete.addActionListener(ae -> {
			try {
				int id = Integer.parseInt(idField.getText());
				PreparedStatement ps = DB.conn.prepareStatement("DELETE FROM courseobjective WHERE ID = ?");
				ps.setInt(1, id);
				ps.executeUpdate();
				JOptionPane.showMessageDialog(null, "Record Deleted.");
				idField.setText("");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Delete Failed.");
			}
		});

		form.add(new Label("Enter ID to Delete:"));
		form.add(idField);
		form.add(new Label(""));
		form.add(delete);

		actionPanel.add(scroll, BorderLayout.CENTER);
		actionPanel.add(form, BorderLayout.SOUTH);
	}
}

public class CodeBrew_CourseObjective {
	public static void main(String[] args) {
		new CourseApp();
	}
}
