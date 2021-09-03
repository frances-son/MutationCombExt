package modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class DbManager {
    Connection conn = null;
    Statement st = null;
    String addr, user, pwd;

    public DbManager(String addr, String userName, String pwd) {
        this.addr = addr;
        this.user = userName;
        this.pwd = pwd;
    }

    public Statement connect_db() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(this.addr, this.user, this.pwd);
            st = conn.createStatement();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return st;
    }

    public void fill_cna_tb(Statement st, String filename, String table_name) {
        File file = new File(filename);
        String line = null, hgnc_symbol = null, old_value = null, sample_id = null, cna = null, value = null,
                query = null;
        String[] splited = null, cna_arr = null;
        ArrayList<String> sample_id_list = new ArrayList<>();
        ArrayList<String> cna_list = new ArrayList<>();
        int size = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                splited = line.split("\t");
                size = splited.length - 1;
                // ** dataset�� ���� �ٸ� ����!!
                if (line.contains("Hugo_Symbol")) { // if first line
                    for (String sampleId : splited) {
                        if (!sampleId.equals("Hugo_Symbol")) { // not 1x1 column
                            sample_id_list.add(sampleId);
                        }
                    }

                    for (int i = 0; i < size; i++) {
                        cna_list.add("NA");
                    }

                } else { // not first line
                    hgnc_symbol = null;
                    for (int i = 0; i < size; i++) {
                        if (i == 0) { // first column
                            hgnc_symbol = splited[i];
                        } else { // other columns
                            if (splited[i].contains("2")) {
                                if (!cna_list.get(i - 1).equals("NA")) {
                                    old_value = cna_list.get(i - 1);
                                    cna_list.set(i - 1, old_value + "/" + hgnc_symbol + ":" + splited[i]);
                                } else {
                                    cna_list.set(i - 1, hgnc_symbol + ":" + splited[i]);
                                }
                            }
                        }
                    } // for loop end
                } // if-else end
            } // while loop end

            // fill table with data
            for (int i = 0; i < size; i++) {
                cna = cna_list.get(i);
                if (!cna.equals("NA")) {
                    sample_id = sample_id_list.get(i);
                    if (cna.contains("/")) { // multiple cna value
                        cna_arr = cna.split("/");
                        for (String item : cna_arr) {
                            hgnc_symbol = item.split(":")[0];
                            value = item.split(":")[1];
                            query = "insert into " + table_name + " values('" + sample_id + "','" + hgnc_symbol + "',"
                                    + value + ")";
                            st.executeUpdate(query);
                        }
                    } else {
                        hgnc_symbol = cna.split(":")[0];
                        value = cna.split(":")[1];
                        query = "insert into " + table_name + " values('" + sample_id + "','" + hgnc_symbol + "',"
                                + value + ")";
                        st.executeUpdate(query);
                    }
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("fill_cna_tb() is done");
    }

    public void fill_mutation_tb(Statement st, String filename, String table_name) {
        BufferedReader br = null;
        String line = null, gene = null, sample_id = null;
        String[] splited = null;
        String[] values = new String[3];
        int splited_size = 0;
        ArrayList<String> sample_ids = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(filename));
            // first line
            line = br.readLine();
            splited = line.split("\t");
            splited_size = splited.length;
            for (int i = 1; i < splited_size; i++) {
                sample_ids.add(splited[i]);
            }
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                gene = splited[0];
                for (int i = 1; i < splited_size; i++) {
                    if (!splited[i].equals("NaN")) {
                        sample_id = sample_ids.get(i - 1);
                        values[0] = sample_id;
                        values[1] = gene;
                        if (splited[i].contains("\"")) {
                            splited[i] = splited[i].replaceAll("\"", "");
                        }
                        values[2] = splited[i];
                        // System.out.println(make_insert_query(table_name, values));
                        st.executeUpdate(make_insert_query(table_name, values));
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void make_selected_mutsig_mutation_tb(Statement st, String dataset_name, String CBioPortal_first_fileName) {
        ResultSet rs = null;
        BufferedReader br = null;
        String only_one_sample_patient_query = "SELECT B.sample_id FROM selected_patient_except_fusion as A INNER JOIN clinical_sample_tb as B on A.patient_id = B.patient_id WHERE (A.sample_count = 1) and (B.sequence_assay_id not like '%solidtumor%') and (B.sequence_assay_id not like '%myeloid%')";
        String insert_query = null;
        String line;
        String[] splited = null;
        HashSet<String> query_set = new HashSet<>();

        try {
            // ** only_one_sample_patient_query to data
            rs = st.executeQuery(only_one_sample_patient_query);
            while (rs.next()) {
                // insert to db
                insert_query = this.make_insert_query_one_value("selected_sample", rs.getString(1).trim());
                query_set.add(insert_query);
            }

            // ** more_one_sample_patient
            br = new BufferedReader(new FileReader(CBioPortal_first_fileName));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                if (splited.length > 2) {
                    if (splited[2].trim().equals("TRUE")) {
                        // insert to db
                        insert_query = this.make_insert_query_one_value("selected_sample", splited[1].trim());
                        query_set.add(insert_query);
                    }
                }
            }

            for (String query : query_set) {
                st.executeUpdate(query);
                // System.out.println(query);
            }

            rs.close();
            st.close();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

    }

    public String make_update_query_same_column(String table_name, String column, String new_value, String old_value) {
        String query = "UPDATE " + table_name + " SET " + column + " = '" + new_value + "' WHERE " + column + " = '"
                + old_value + "';";
        return query;
    }

    public String make_update_query_diff_column(String table_name, String col_update, String col2_condition,
                                                String new_value, String old_value) {
        String query = "UPDATE " + table_name + " SET " + col_update + " = '" + new_value + "' WHERE " + col2_condition
                + " = '" + old_value + "';";
        return query;
    }

    public String make_delete_query(String table_name, String column, String target_value) {
        String query = "DELETE from " + table_name + " where " + column + " = '" + target_value + "';";
        return query;
    }

    public String make_insert_query(String table_name, String[] values) {
        int val_size = values.length;
        StringBuilder sb = new StringBuilder("INSERT INTO " + table_name + " VALUES (");
        for (int i = 0; i < val_size; i++) {
            sb.append("\'");
            sb.append(values[i]);
            sb.append("\'");
            if (i != val_size - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public String make_select_query_specific_values(String table_name, String[] col_names, String where_condition,
                                                    String order_condition) {
        StringBuilder sb = new StringBuilder("SELECT ");
        int col_size = col_names.length;
        for (int i = 0; i < col_size; i++) {
            sb.append(col_names[i]);
            if (i != col_size - 1) {
                sb.append(",");
            }
        }
        sb.append(" FROM ");
        sb.append(table_name);
        if (!where_condition.equals("")) {
            sb.append(" WHERE ");
            sb.append(where_condition);
        }

        if (order_condition != null) {
            sb.append(" ");
            sb.append(order_condition);
        }

        return sb.toString();
    }

    public String make_select_query_specific_values(String table_name, String[] col_names, String where_condition) {
        StringBuilder sb = new StringBuilder("SELECT ");
        int col_size = col_names.length;
        for (int i = 0; i < col_size; i++) {
            sb.append(col_names[i]);
            if (i != col_size - 1) {
                sb.append(",");
            }
        }
        sb.append(" FROM ");
        sb.append(table_name);
        if (!where_condition.equals("")) {
            sb.append(" WHERE ");
            sb.append(where_condition);
        }
        return sb.toString();

    }

    public String make_insert_query_specific_values(String table_name, String[] col_names, String[] values) {
        int val_size = values.length;
        int col_size = col_names.length;
        StringBuilder sb = new StringBuilder("INSERT INTO " + table_name + "(");

        for (int i = 0; i < col_size; i++) {
            sb.append(col_names[i]);
            if (i != val_size - 1) {
                sb.append(",");
            }
        }
        sb.append(") VALUES (");
        for (int i = 0; i < val_size; i++) {
            sb.append("\'");
            sb.append(values[i]);
            sb.append("\'");
            if (i != val_size - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public String make_insert_query_one_value(String table_name, String values) {
        StringBuilder sb = new StringBuilder("INSERT INTO " + table_name + " VALUES ('");
        sb.append(values);
        sb.append("')");
        return sb.toString();
    }

    public String make_patient_sample_query(String patient_id) {
        String qurey = "SELECT sample_id, COUNT(*) as cnt\r\n" + "FROM mutation_tb\r\n" + "WHERE sample_id LIKE '%"
                + patient_id + "%'\r\n" + "GROUP BY sample_id\r\n" + "order by cnt";
        return qurey;
    }

    public void make_update_query_deprecated_oncotree_code(Statement st, String filename, boolean history) {
        HashMap<String, String> old_new_map = new HashMap<>();
        BufferedReader br = null;
        String line = null, first_one, second_one;

        try {
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                first_one = line.split("\t")[0];
                second_one = line.split("\t")[1];
                if (history == true) {
                    System.out.println(make_update_query_same_column("selected_sample_tb", "oncotree_code", second_one,
                            first_one));
                } else {
                    System.out.println(make_update_query_diff_column("mapped_sample_cohort_tb", "cohort",
                            "oncotree_code", second_one, first_one));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void make_delete_queries_from_file(Statement st, String filename) {
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                System.out.println(make_delete_query("cna_genie_tb", "sample_id", line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String make_summit_erbb_select_query(String sample_id, String erbb) {
        String query = "select count(sample_id) from mutation_tb where sample_id = '" + sample_id
                + "' and hgnc_symbol = '" + erbb + "'";
        return query;
    }

    public String make_query_domain_vClass_xref(String domain_source, String[] hgnc_symbol_arr, String sample_id) {
        StringBuilder sb = new StringBuilder("select hgnc_symbol, variant_classification, variant_type," + domain_source
                + " from selected_domains_xref_tb where (sample_id = '");
        sb.append(sample_id);
        sb.append("') and (variant_classification != 'Silent')");

        if (hgnc_symbol_arr != null) {
            sb.append(" and (");
            if (hgnc_symbol_arr.length == 1) {
                sb.append("hgnc_symbol = '");
                sb.append(hgnc_symbol_arr[0]);
                sb.append("')");
            } else {
                for (int i = 0; i < hgnc_symbol_arr.length; i++) {
                    if (i != 0) {
                        sb.append(" or ");
                    }
                    sb.append("hgnc_symbol = '");
                    sb.append(hgnc_symbol_arr[i]);
                    sb.append("'");
                }
                sb.append(")");
            }
        }
        return sb.toString();
    }

    public void mutation_interpro_mapping(Statement st, Statement st_another, Statement st_other) {
        ResultSet rs, rs_2 = null;
        boolean insert_boolean = false;
        String[] variant_classification = {"MISSENSE_MUTATION", "NONSENSE_MUTATION"};
        String[] variant_type = {"SNP", "DNP", "TNP", "ONP"};
        int variant_classifciation_idx = 1;
        int variant_type_idx = 0;
        String query_mutation_tb = "select transcript_id, protein_position, mutation_id, hgnc_symbol from mutation_tb where variant_classification = '"
                + variant_classification[variant_classifciation_idx] + "' and variant_type = '"
                + variant_type[variant_type_idx]
                + "' and exon != '' and ((sample_id LIKE '%MSK%') OR (sample_id LIKE '%DFCI%')) ORDER BY TRANSCRIPT_ID ";
        System.out.println(query_mutation_tb);
        String query_interpro_genie_tb, transcript_id, protein_position, interpro_start, interpro_end, mutation_id,
                hgnc_symbol, genie_interpro_id = null, interpro_id = null, before_transcript_id = "init";
        int position = -1;
        ArrayList<String[]> list_specific_transcript_id = new ArrayList<>();
        // int count = 0;
        try {
            rs = st.executeQuery(query_mutation_tb);
            while (rs.next()) {
                transcript_id = rs.getString(1);
                protein_position = rs.getString(2);
                mutation_id = rs.getString(3);
                hgnc_symbol = rs.getString(4);
                // System.out.println("current transcript_id : " + transcript_id);
                // System.out.println("before transcript_id : " + before_transcript_id);
                if (!before_transcript_id.equals(transcript_id) || before_transcript_id.equals("init")) {
                    // �������� �Ⱦ��°� �ƴ��� Ȯ���ؾ��� - Ȯ����
                    // System.out.println("new");
                    // ** if new transcript_id
                    list_specific_transcript_id.clear();
                    query_interpro_genie_tb = "select interpro_start, interpro_end, genie_interpro_id, interpro_id from interpro_genie_tb where transcript_id ='"
                            + transcript_id + "'";
                    rs_2 = st_another.executeQuery(query_interpro_genie_tb);

                    // put results that related to specific transcript_id to arraylist
                    while (rs_2.next()) {
                        interpro_start = rs_2.getString(1);
                        interpro_end = rs_2.getString(2);
                        genie_interpro_id = rs_2.getString(3);
                        interpro_id = rs_2.getString(4);
                        // System.out.println(
                        // interpro_start + "/" + interpro_end + "/" + genie_interpro_id + "/" +
                        // interpro_id);
                        if (interpro_start.length() != 0 && interpro_end.length() != 0) {
                            list_specific_transcript_id
                                    .add(new String[]{interpro_start, interpro_end, genie_interpro_id, interpro_id});
                        }
                    }
                }

                if (list_specific_transcript_id.size() != 0) {
                    if (protein_position.contains("/")) { // It has "specific position/positionlength" position value
                        String query = null;
                        // ArrayList<String> multi_point_IPR_arr = new ArrayList<>();
                        // ArrayList<String> multi_point_IG_arr = new ArrayList<>();
                        for (String[] arr : list_specific_transcript_id) {
                            int start = Integer.valueOf(arr[0]);
                            int end = Integer.valueOf(arr[1]);

                            if (protein_position.contains("-")) { // protein position has range (multipoint)
                                // TODO : not complete !
                                // non, mis ���� �ʼ�
                                // String[] num_arr = protein_position.split("-");
                                // int prior = Integer.valueOf(num_arr[0]);
                                // int post = Integer.valueOf(num_arr[1]);
                                // for (int num = prior; num <= post; num++) {
                                // if (num >= start && num <= end) {
                                // }
                                // }
                            } else {
                                position = Integer.valueOf(protein_position.split("/")[0]);
                                if (variant_classifciation_idx == 0) { // missesnse mutation
                                    if (position >= start && position <= end) {
                                        // It fits in range
                                        // query = "insert into mapping_interpro_genie values ('" + mutation_id +"','"
                                        // + arr[2] + "','" + arr[3] + "','" + hgnc_symbol + "')";
                                        insert_boolean = true;
                                    }
                                } else if (variant_classifciation_idx == 1) { // nonsense mutation
                                    if ((position >= start && position <= end) || (position <= start)) {
                                        insert_boolean = true;
                                    }
                                }
                                if (insert_boolean) {
                                    query = "insert into mapping_interpro_genie values ('" + mutation_id + "','"
                                            + arr[2] + "','" + arr[3] + "','" + hgnc_symbol + "')";
                                    st_other.executeUpdate(query);
                                    // System.out.println(query);
                                    insert_boolean = false;
                                }
                            }
                        }
                    }
                }
                before_transcript_id = transcript_id;
                // if (count == 40)
                // System.exit(0);
                // count++;
            }
            rs.close();
            rs_2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void summit_no_erbb_insert_data(Statement st, String summit_clinical_file) {
        ResultSet rs = null;
        BufferedReader br = null;
        String line, sample_id, erbb, qualifying_mutation;
        String[] splited = null, insert_values = new String[4], insert_colum_names = new String[4];
        HashSet<String> query_set = new HashSet<>();

        try {
            insert_colum_names[0] = "sample_id";
            insert_colum_names[1] = "hgnc_symbol";
            insert_colum_names[2] = "hgvsp_short";
            insert_colum_names[3] = "variant_classification";

            br = new BufferedReader(new FileReader(summit_clinical_file));
            br.readLine();
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");

                sample_id = splited[4];
                erbb = splited[24];
                qualifying_mutation = splited[33];

                rs = st.executeQuery(make_summit_erbb_select_query(sample_id, erbb));
                // System.out.println(make_summit_erbb_select_query(sample_id, erbb));
                while (rs.next()) {

                    if (rs.getInt(1) < 1) {
                        insert_values[0] = sample_id;
                        insert_values[1] = erbb;
                        insert_values[2] = qualifying_mutation;
                        insert_values[3] = "manual";

                        // System.out.println(sample_id);
                        // System.out.println(make_insert_query_specific_values("mutation_tb",
                        // insert_colum_names, insert_values));
                        query_set.add(
                                make_insert_query_specific_values("mutation_tb", insert_colum_names, insert_values));
                    }
                }
            }
            rs.close();

            for (String sql : query_set) {
                st.executeUpdate(sql);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }
}
