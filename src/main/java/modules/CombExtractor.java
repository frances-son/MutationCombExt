package modules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import util.JwUtil;

public class CombExtractor {

//    JwUtil util = new JwUtil();
//    public static HashMap<String, String> item_and_idx_map = new HashMap<>();
//    public static HashMap<String, String> idx_and_item_map = new HashMap<>();
//    public static HashMap<String[], String> item_arr_and_sample_id_map = new HashMap<>();
//    QueryToDb dbo = new QueryToDb("jdbc:mysql://localhost/genie_7_0?serverTimezone=UTC", "root", "1541");
//    public static String idx_item_map_file = "C:\\genie_7_0\\output\\20200226_140419_item-idx_mapping_list.tsv";
//    //	public static String item_arr_and_sample_id_map_file = "C:\\genie_6_1\\output\\item_idx_map_20190916_094129.tsv";
//    public static String[] need_col_arr = new String[]{"hgnc_symbol", "refseq", "variant_classification",
//            "variant_type", "start_position", "end_position", "reference_allele", "tumor_seq_allele1",
//            "tumor_seq_allele2", "protein_position", "hgvsp_short", "dbsnp_rs"};
//    public String oncokb_path = "C:\\genie_7_0\\oncokb_allAnnotatedVariants_191214.txt";
//    public static HashMap<String, Integer> inner_duplicated_map = new HashMap<>();
//    public HashMap<String, String[]> type_name_and_inner_duplicated_map = new HashMap<>();
//
//    public static void load_item_arr_and_sample_id_map() {
//        BufferedReader br = null;
//        String line = null;
//        String[] splited = null;
//        try {
//            br = new BufferedReader(new FileReader(idx_item_map_file));
//            while ((line = br.readLine()) != null) {
//                splited = line.split("\t");
//                item_arr_and_sample_id_map.put(Arrays.copyOfRange(splited, 1, splited.length), splited[0]);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void load_item_and_idx_map() {
//        BufferedReader br = null;
//        String line = null;
//        String[] splited = null;
//        try {
//            br = new BufferedReader(new FileReader(idx_item_map_file));
//            while ((line = br.readLine()) != null) {
//                splited = line.split("\t");
//                item_and_idx_map.put(splited[1].trim(), splited[0].trim());
//                idx_and_item_map.put(splited[0].trim(), splited[1].trim());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void map_items_to_idx_from_all_files(String folder_path) {
//        BufferedReader br = null;
//        BufferedWriter bw = null;
//        String line = null;
//        String[] splited = null;
//        String item = null;
//        String time_log = util.get_time_log();
//        int idx_cnt = -1;
//
//        File path = new File(folder_path);
//        File[] files_from_folder = path.listFiles();
//        HashSet<String> items_set = new HashSet<>();
//
//        try {
//            bw = new BufferedWriter(new FileWriter("C:\\genie_6_1\\output\\" + time_log + "_item_idx_map.tsv"));
//            for (File file : files_from_folder) {
//                br = new BufferedReader(new FileReader(file));
//                while ((line = br.readLine()) != null) {
//                    splited = line.split("\t");
//                    for (int i = 3; i < splited.length; i++) {
//                        item = splited[i];
//                        if (!items_set.contains(item)) {
//                            idx_cnt++;
//                            bw.write(String.valueOf(idx_cnt) + "\t" + item + "\n");
//                            bw.flush();
//                        }
//                        items_set.add(item);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Done!");
//    }
//
//    public int count_patients_in_specific_folder(String folder_path) {
//        File path = new File(folder_path);
//        BufferedReader br = null;
//        File[] files_from_folder = path.listFiles();
//        String line = null;
//        int sum = 0;
//        for (File file : files_from_folder) {
//            try {
//                br = new BufferedReader(new FileReader(file));
//                while ((line = br.readLine()) != null) {
//                    sum++;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        // System.out.println(Integer.valueOf(sum));
//        return sum;
//    }
//
//    public void indexing_all_files(String folder_path, String index_mapping_file, String project_name) {
//        BufferedReader br = null;
//        BufferedWriter bw = null;
//        String line = null, item = null, file_name = null, new_file_name = null, new_path = null, idx = null;
//        String[] splited = null;
//        String time_log = util.get_time_log();
//
//        File path = new File(folder_path);
//        File[] files_from_folder = path.listFiles();
//        HashMap<String, String> item_idx_map = new HashMap<>();
//
//        try {
//            new_path = "C:\\" + project_name + "\\output\\" + time_log + "_indexed_files";
//            new File(new_path).mkdirs();
//
//            // # STEP 1. read index_mapping_file
//            br = new BufferedReader(new FileReader(index_mapping_file));
//            while ((line = br.readLine()) != null) {
//                splited = line.split("\t");
//                item_idx_map.put(splited[1], splited[0]);
//            }
//
//            // # STEP 2. covert item to index from all files
//            for (File file : files_from_folder) {
//                br = new BufferedReader(new FileReader(file));
//                file_name = file.getName().split("\\.")[0];
//                file_name = file_name.split("_")[0];
//
//                new_file_name = new_path + "\\indexed_" + file_name + "_" + time_log + ".dat";
//                new_file_name = new_file_name.replaceAll(" ", "_");
//
//                bw = new BufferedWriter(new FileWriter(new_file_name));
//
//                while ((line = br.readLine()) != null) {
//                    splited = line.split("\t");
//                    for (int i = 3; i < splited.length; i++) {
//                        idx = item_idx_map.get(splited[i]);
//                        bw.write(idx);
//                        if (i != splited.length - 1) {
//                            bw.write(" ");
//                        }
//                    }
//                    bw.write("\n");
//                    bw.flush();
//                }
//
//            }
//            // bw.close();
//            br.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        System.out.println("Done!");
//    }
//
//    public void split_one_cancer_type_by_oncotree(String file_path, String new_folder_path) {
//        BufferedReader br = null;
//        BufferedWriter bw = null;
//        String line = null;
//        String[] splited = null;
//        HashMap<String, BufferedWriter> oncotree_bw_map = new HashMap<>();
//        String idx;
//
//        try {
//            // ** load idx_item_map
//            this.load_item_and_idx_map();
//
//            br = new BufferedReader(new FileReader(file_path));
//            while ((line = br.readLine()) != null) {
//                splited = line.split("\t");
//                if (!oncotree_bw_map.containsKey(splited[1])) {
//                    oncotree_bw_map.put(splited[1],
//                            new BufferedWriter(new FileWriter(new_folder_path + "\\" + splited[1] + ".tsv")));
//                }
//                bw = oncotree_bw_map.get(splited[1]);
//
//                for (int i = 3; i < splited.length; i++) {
//                    idx = GetCombination.item_and_idx_map.get(splited[i]);
//                    bw.write(idx);
//                    if (i != splited.length - 1) {
//                        bw.write(" ");
//                    }
//                }
//                bw.write("\n");
//                bw.flush();
//            }
//            System.out.println("done!");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void make_console_commands(String folder_path) {
//        new File(folder_path + "\\gMiner_console_command").mkdir();
//        File path = new File(folder_path);
//        File[] files_from_folder = path.listFiles();
//        String file_name = null;
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(
//                    folder_path + "\\gMiner_console_command\\gminer_cmd_" + util.get_time_log() + ".sh"));
//
//            for (File file : files_from_folder) {
//                if (!file.isDirectory()) {
//                    file_name = file.getName();
//                    String running_msg = " echo [GMiner] running... " + file_name;
//                    String cmd = "./GMiner -i input/" + file_name + " -o output/" + file_name.split("\\.")[0] + ".out"
//                            + " -g 2 -s 0.009 -w 1 >> output/log_" + file_name.split("\\.")[0] + ".log";
//
//                    bw.write(running_msg + "\n");
//                    System.out.println(running_msg);
//                    bw.flush();
//                    bw.write(cmd + "\n");
//                    System.out.println(cmd);
//                    bw.flush();
//                }
//            }
//            bw.write("echo ***************Done!***************");
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void indexs_to_item_and_support_value(String folder_path) {
//        GetCombination.load_item_and_idx_map();
//        File path = new File(folder_path);
//        File[] files_from_folder = path.listFiles();
//        BufferedReader br = null;
//        BufferedWriter bw = null;
//        String line, time_log = util.get_time_log(), file_name, support_value = null, item = null;
//        String[] splited = null;
//        HashMap<String, String> oncotree_type_patient_cnt_map = new HashMap<>();
//        double real_cnt = 0.0, patinet_cnt_per_oncotree_code = 0.0;
//
//        for (File file : files_from_folder) {
//            if (file.getName().contains(".out")) {
//                try {
//                    br = new BufferedReader(new FileReader(folder_path + "\\cnt.txt"));
//                    while ((line = br.readLine()) != null) {
//                        splited = line.split("\t");
//                        oncotree_type_patient_cnt_map.put(splited[0], splited[1]);
//                    }
//                    System.out.println(oncotree_type_patient_cnt_map);
//
//                    br = new BufferedReader(new FileReader(file));
//                    file_name = file.getName().split("\\_")[0];
//                    System.out.println(file_name);
//                    patinet_cnt_per_oncotree_code = (double) Integer
//                            .valueOf(oncotree_type_patient_cnt_map.get(file_name));
//
//                    System.out.println(patinet_cnt_per_oncotree_code);
//                    bw = new BufferedWriter(new FileWriter(folder_path + "\\" + file_name + "_" + time_log + ".tsv"));
//
//                    while ((line = br.readLine()) != null) {
//                        support_value = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
//                        real_cnt = Math.round(Double.valueOf(support_value) * patinet_cnt_per_oncotree_code);
//
//                        if (real_cnt >= 3.00) {
//                            splited = line.split("\\(");
//                            splited = splited[0].split(" ");
//                            Arrays.sort(splited);
//
//                            for (int i = 0; i < splited.length; i++) {
//                                item = idx_and_item_map.get(splited[i]);
//                                bw.write(item);
//                                if (i != splited.length - 1) {
//                                    bw.write(",");
//                                }
//                            }
//                            bw.write("\t");
//                            bw.write(support_value);
//                            bw.write("\t");
//                            real_cnt = Math.round(Double.valueOf(support_value) * patinet_cnt_per_oncotree_code);
//                            bw.write(String.valueOf(real_cnt));
//                            bw.write("\n");
//                            bw.flush();
//                        }
//                    }
//                    bw.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        System.out.println("Done!");
//    }
//
//    public void get_hgvsp_short_and_variant_classification_by_combination(String combination_folder,
//                                                                          String splited_patient_list_folder, int count_patient_sum_of_specific_folder) {
//
//        // ** db connect
//        GetCombination.load_item_arr_and_sample_id_map();
//        BufferedReader combination_br, patient_br = null;
//        BufferedWriter bw = null;
//        String[] comb_splited = null, patient_item_arr = null, inner_splited = null, comb_items_arr = null;
//        ArrayList<String> patient_file_names = new ArrayList<>(), patient_line_from_file_list = new ArrayList<>(),
//                mut_items_arr = new ArrayList<>();
//        ArrayList<String[]> returned_sql_value_list = null;
//        HashMap<String, ArrayList<String[]>> sample_id_and_returned_values_list_of_specific_combination_list_map = new HashMap<>();
//        HashMap<String, ArrayList<String>> gathered_by_types_of_combination_and_sample_id_map = new HashMap<>();
//        HashMap<String, String> oncokb_hgnc_and_info_map = new HashMap<>();
//        ArrayList<StringBuilder> new_line_for_writing = new ArrayList<>();
//        File new_folder = new File(combination_folder + "\\granular_analysis");
//        OncokbReader oncokb_reader = new OncokbReader();
//
//        int file_idx = 0, cnv_cnt = 0, comb_cnt = 0;
//        int[] cnv_items_idx_arr = null;
//        boolean check_point = false;
//        String prob_val, cnt_val, cnv_value, time_log;
//        Statement st = dbo.connect_db();
//        StringBuilder sb_cnv = null;
//        String comb_cnt_by_upper_cancer_type_cnt = null;
//
//        String comb_line = null, sample_id = null, query = null, comb_file_oncotree = null, patient_line = null;
//        StringBuilder where_condition = null;
//        File patient_file = null;
//
//        File combination_folder_path = new File(combination_folder);
//        File[] files_from_combination_folder = combination_folder_path.listFiles();
//
//        File splited_patient_list_folder_path = new File(splited_patient_list_folder);
//        File[] files_from_patient_folder_name = splited_patient_list_folder_path.listFiles();
//
//        String order_condition = "ORDER BY hgnc_symbol, hgvsp_short asc";
//
//        // ** Before make file to write, load oncoKB data and fill data
//        oncokb_hgnc_and_info_map = oncokb_reader.oncokb_file_read(oncokb_path);
//
//        for (File pat_file : files_from_patient_folder_name) {
//            patient_file_names.add(pat_file.getName().split("_")[0]);
//        }
//
//        try {
//            time_log = util.get_time_log();
//            new File(new_folder.toString() + "\\" + time_log).mkdir();
//
//            for (File comb_file : files_from_combination_folder) {
//                System.out.println(comb_file.getName());
//
//                if (comb_file.getName().contains(".tsv")) {
//                    combination_br = new BufferedReader(new FileReader(comb_file));
//                    comb_file_oncotree = comb_file.getName().split("_")[0];
//
//                    // ** make BufferedWriter
//                    bw = new BufferedWriter(new FileWriter(new_folder.toString() + "\\" + time_log + "\\"
//                            + comb_file_oncotree + "_GA_" + time_log + ".tsv"));
//                    // ** write first line
//                    bw.write(
//                            "inner_duplicated\tinner_duplicated_cnt_sum\tinner_duplicated_cnt_per_gene\ttype_name\ttype_num\tsample_cnt_per_type\ttype_cnt_devided_by_comb_cnt\ttype_cnt_devided_by_upper_cancer_type\ttype_cnt_devided_by_whole_cnt\tsample_id\tHugo_Symbol\tRefseq\tVariant_Classification\tVariant_Type\tStart_Position\tEnd_Position\tReference_Allele\tTumor_Seq_Allele1\tTumor_Seq_Allele2\tProtein_position\tHGVSp_Short\tdbSNP_RS\tcombination\titem_cnt\tcomb_prob_by_specific_cancer_type\tcomb_cnt\tcomb_prob_by_upper_cancer_type");
//                    bw.write("\n");
//                    file_idx = patient_file_names.indexOf(comb_file_oncotree);
//
//                    if (file_idx == -1) {
//                        System.out.println("[Check Again] There is no " + comb_file_oncotree + " in patients folder.");
//                    } else {
//                        patient_file = files_from_patient_folder_name[file_idx];
//                        patient_br = new BufferedReader(new FileReader(patient_file));
//
//                        patient_line_from_file_list.clear();
//                        while ((patient_line = patient_br.readLine()) != null) {
//                            patient_line_from_file_list.add(patient_line);
//                        }
//                        gathered_by_types_of_combination_and_sample_id_map.clear();
//
//                        while ((comb_line = combination_br.readLine()) != null) {
//                            sample_id_and_returned_values_list_of_specific_combination_list_map.clear();
//                            // System.out.println(comb_line);
//
//                            // ** ���� dataset ����
//                            new_line_for_writing.clear();
//
//                            // System.out.println(
//                            // "-----------------------------------------------------------------------------------");
//                            // System.out.println(comb_line);
//                            // It prints 'gene name / probability value / count
//
//                            // ** get combination file using oncotree name
//                            comb_splited = comb_line.split("\t");
//
////							System.out.println(Arrays.deepToString(comb_splited));
//
//                            // ** save prob val, cnt val
//                            prob_val = comb_splited[1];
//                            cnt_val = comb_splited[2];
//
//                            if (comb_splited[0].contains(",")) {
//                                // �������� �������� ��
//                                comb_items_arr = comb_splited[0].split(",");
//                                comb_cnt = comb_items_arr.length;
//                            } else {
//                                // �������� �ϳ��� ��
//                                comb_items_arr = new String[]{comb_splited[0]};
//                                comb_cnt = 1;
//                            }
//
//                            // ** �ش� combination�� ���ؼ� ȯ�� ���̵� ����Ʈ ��������
//                            for (String inner_line : patient_line_from_file_list) {
//                                inner_splited = inner_line.split("\t");
//                                sample_id = inner_splited[0];
//                                patient_item_arr = Arrays.copyOfRange(inner_splited, 3, inner_splited.length);
//
//                                // ** if patient has these item combination
//                                if (util.arr_contains_other_arr(patient_item_arr, comb_items_arr)) { // if open
//                                    mut_items_arr.clear();
//                                    // **** CNV ó��
//                                    for (int i = 0; i < comb_items_arr.length; i++) {
//                                        if (comb_items_arr[i].contains("_AMP") || comb_items_arr[i].contains("_DEL")) {
//                                            // *** if it is cnv item
//                                            // ** if patient has these item combination
//                                            cnv_value = comb_items_arr[i].split("_")[1];
//                                            comb_cnt_by_upper_cancer_type_cnt = String.valueOf(Double.valueOf(cnt_val)
//                                                    / (double) count_patient_sum_of_specific_folder);
//                                            sb_cnv = new StringBuilder();
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(comb_items_arr[i]);
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(sample_id);
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(comb_items_arr[i].split("_")[0]);
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(cnv_value);
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(comb_splited[0]);
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(comb_cnt);
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(prob_val);
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(cnt_val);
//                                            // added 2019.12.07
//                                            sb_cnv.append("\t");
//                                            sb_cnv.append(comb_cnt_by_upper_cancer_type_cnt);
//                                            // added 2019.12.07 end
//                                            // added 2019.12.14 _ oncokb data
//                                            // ����� ������ : comb_items_arr[i].split("_")[0] + cnv_value
//
//                                            // added 2019.12.14 end
//
//                                            bw.write(sb_cnv.toString());
//                                            bw.write("\n");
//                                            bw.flush();
//                                        } else {
//                                            mut_items_arr.add(comb_items_arr[i]);
//                                        }
//                                    } // CNV ó�� �� (CNVó���� ���ڸ��� �������.(�� Sample_id ����)
//
//                                    // **** MUT ó��
//                                    if (mut_items_arr.size() >= 1) {
//                                        where_condition = new StringBuilder();
//                                        where_condition.append("sample_id='");
//                                        where_condition.append(sample_id);
//                                        where_condition.append("' AND (");
//
//                                        for (int i = 0; i < mut_items_arr.size(); i++) {
//                                            where_condition.append("hgnc_symbol = ");
//                                            where_condition.append("'");
//                                            where_condition.append(mut_items_arr.get(i));
//                                            where_condition.append("'");
//
//                                            if (i != mut_items_arr.size() - 1) {
//                                                where_condition.append(" OR ");
//                                            }
//                                        }
//                                        where_condition.append(")");
//
//                                        query = dbo.make_select_query_specific_values("mutation_tb", need_col_arr,
//                                                where_condition.toString(), order_condition);
//
////										System.out.println(query);
//
//                                        // ** send query to database
//                                        returned_sql_value_list = this.get_returned_value_using_query(query, st);
////										System.out.println(returned_sql_value_list.toString());
//
//                                        // ** STEP 1. �� combination (gene set) ���� sample_id�� DB�� ���� ���� �����͸� �����ϱ�
//                                        sample_id_and_returned_values_list_of_specific_combination_list_map
//                                                .put(sample_id, returned_sql_value_list);
//                                    }
//                                }
//                            }
//
//                            if (!sample_id_and_returned_values_list_of_specific_combination_list_map.isEmpty()) {
//                                // ** inner duplicated will be counted here
//                                // ((using static data type(inner_duplicated_map)
//
//                                gathered_by_types_of_combination_and_sample_id_map = get_granul_from_returned_values(
//                                        sample_id_and_returned_values_list_of_specific_combination_list_map);
//
//                                // // ** STEP 2. �� combination�� ���� �� ȯ���� variant�� granular analysis �ϱ�
//                                // gathered group ���� Ÿ���� �ű� �� �켱��... (10/14) ��ü column�� �ẻ��. hgvsp �������� ����.
//                                new_line_for_writing.clear();
//
//                                new_line_for_writing = extract_line_for_writing_file(
//                                        gathered_by_types_of_combination_and_sample_id_map,
//                                        sample_id_and_returned_values_list_of_specific_combination_list_map, prob_val,
//                                        cnt_val, count_patient_sum_of_specific_folder);
//
////								 System.out.println("new_line_for_writing");
//                                // System.out.println(new_line_for_writing.size());
////								 System.out.println(new_line_for_writing.toString());
//
//                                for (StringBuilder sb : new_line_for_writing) {
//                                    sb.append("\t");
//                                    sb.append(comb_splited[0]);
//                                    sb.append("\t");
//                                    sb.append(comb_cnt);
//                                    sb.append("\t");
//                                    sb.append(prob_val);
//                                    sb.append("\t");
//                                    sb.append(cnt_val);
//                                    // added 2019.12.07
//                                    sb.append("\t");
//                                    sb.append(String.valueOf(
//                                            Double.valueOf(cnt_val) / (double) count_patient_sum_of_specific_folder));
//
////									if (comb_splited[0].equals("CDH1")) {
////										System.out.println(sb.toString());
////									}
//
//                                    // added end 2019.12.07
//                                    bw.write(sb.toString() + "\n");
//                                    bw.flush();
//                                }
//
//                            }
//
//                        }
//                        // System.exit(0);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Done!");
//    }
//
//    public ArrayList<String[]> get_returned_value_using_query(String query, Statement st) {
//        ArrayList<String[]> list_to_return = new ArrayList<>();
//        String[] inner_arr = null;
//        try {
//            ResultSet rs = st.executeQuery(query);
//            while (rs.next()) {
//                inner_arr = new String[need_col_arr.length];
//                for (int i = 1; i <= need_col_arr.length; i++) {
//                    inner_arr[i - 1] = rs.getString(i);
//                }
//                list_to_return.add(inner_arr);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return list_to_return;
//    }
//
//    public HashMap<String, ArrayList<String>> get_granul_from_returned_values(
//            HashMap<String, ArrayList<String[]>> returned_map) {
//        HashMap<String, ArrayList<String>> gene_hgvsp_and_sample_id_map = new HashMap<>();
//
//        StringBuilder mixed_key = null;
//        StringBuilder gene_and_duplicated_cnt = null;
//        int inner_duplication_cnt_sum = 0;
//
//        for (String sample_id : returned_map.keySet()) {
//            // **** type_name ���ϴ� �κ� ****
//            mixed_key = new StringBuilder();
//            inner_duplicated_map.clear();
//            for (String[] patient_info_arr : returned_map.get(sample_id)) {
//                // ** check inner duplication (using static data type(inner_duplicated_map))
//                if (!inner_duplicated_map.containsKey(patient_info_arr[0])) {
//                    inner_duplicated_map.put(patient_info_arr[0], 1);
//                } else {
//                    inner_duplicated_map.replace(patient_info_arr[0],
//                            inner_duplicated_map.get(patient_info_arr[0]) + 1);
//                }
//                mixed_key.append(patient_info_arr[0]);
//                mixed_key.append("_");
//                if (patient_info_arr[10].length() > 1) {
//                    mixed_key.append(patient_info_arr[10]);
//                    mixed_key.append(",");
//                } else {
//                    mixed_key.append("unknown");
//                    mixed_key.append(",");
//                }
//            }
////			System.out.println(sample_id);
////			System.out.println(mixed_key);
//            mixed_key.deleteCharAt(mixed_key.length() - 1);
//
//            inner_duplication_cnt_sum = 0;
//            gene_and_duplicated_cnt = new StringBuilder();
//            for (String gene : inner_duplicated_map.keySet()) {
//                if (inner_duplicated_map.get(gene) != 1) {
//                    inner_duplication_cnt_sum += inner_duplicated_map.get(gene);
//                }
//                gene_and_duplicated_cnt.append(gene + "_" + Integer.valueOf(inner_duplicated_map.get(gene)));
//                gene_and_duplicated_cnt.append(",");
//            }
//
//            gene_and_duplicated_cnt.deleteCharAt(gene_and_duplicated_cnt.length() - 1);
//            type_name_and_inner_duplicated_map.put(mixed_key.toString(),
//                    new String[]{gene_and_duplicated_cnt.toString(), String.valueOf(inner_duplication_cnt_sum)});
//            // ** 2019.12.06 : inner_duplication_cnt_sum�� inner_duplicated T/F�� ������ �Ǿ��.
//
//            if (gene_hgvsp_and_sample_id_map.containsKey(mixed_key.toString())) {
//                // if mixed_key (gene symbol+hgvsp_short) is already exist, add sample_id
//                gene_hgvsp_and_sample_id_map.get(mixed_key.toString()).add(sample_id);
//            } else {
//                gene_hgvsp_and_sample_id_map.put(mixed_key.toString(), new ArrayList<>());
//                gene_hgvsp_and_sample_id_map.get(mixed_key.toString()).add(sample_id);
//            }
//        }
//
//        return gene_hgvsp_and_sample_id_map;
//    }
//
//    public ArrayList<StringBuilder> extract_line_for_writing_file(
//            HashMap<String, ArrayList<String>> gathered_by_types_of_combination_and_sample_id_map,
//            HashMap<String, ArrayList<String[]>> sample_id_and_returned_values_list_of_specific_combination_list_map,
//            String whole_prob_val, String whole_cnt_val, int count_patient_sum_of_specific_folder) {
//        StringBuilder new_line = null;
//        ArrayList<String[]> mutation_data_list_from_db = null;
//        ArrayList<StringBuilder> list_to_return = new ArrayList<>();
//        int type_num = 0, cnt_per_type = 0;
//        double type_cnt_devided_whole_cnt = 0.0;
//        // ** 2019.12.06 "is_duplicated_mutation"�� T/F�� ������ Pattern �̾��µ�... �̰ź��� �׳�
//        // ** inner_duplicated_cnt_sum �̰ſ� ��뼭 ���°� �� ������ �� ����. �ٵ� �� �� ����� ó������ ��������?
//        boolean is_duplicated_mutation = false;
//        // Pattern pattern = Pattern.compile("._[2-9].");
//        // Matcher matcher = null;
//
//        for (String type_name : gathered_by_types_of_combination_and_sample_id_map.keySet()) {
//            is_duplicated_mutation = false;
//
//            // matcher =
//            // pattern.matcher(type_name_and_inner_duplicated_map.get(type_name)[0]);
//            if (Integer.valueOf(type_name_and_inner_duplicated_map.get(type_name)[1]) >= 2) {
//                is_duplicated_mutation = true;
//                // System.out.println("hello");
//            }
//
//            // ** type_name example : IGF1R_p.E1342D,TP53_p.Q165*
//            type_num++;
//            cnt_per_type = gathered_by_types_of_combination_and_sample_id_map.get(type_name).size();
//            type_cnt_devided_whole_cnt = cnt_per_type / Double.valueOf(whole_cnt_val);
//
//            for (String sample_id : gathered_by_types_of_combination_and_sample_id_map.get(type_name)) {
//                mutation_data_list_from_db = sample_id_and_returned_values_list_of_specific_combination_list_map
//                        .get(sample_id);
//                for (String[] column_arr : mutation_data_list_from_db) {
//                    new_line = new StringBuilder();
//                    // TODO
//                    if (is_duplicated_mutation) {
//                        new_line.append("T");
//                    } else {
//                        new_line.append("F");
//                    }
//                    // TODO
//
//                    new_line.append("\t");
//                    // ex: inner duplicated sum in all gene in this type name (ex : 5)
//                    new_line.append(type_name_and_inner_duplicated_map.get(type_name)[1]);
//                    new_line.append("\t");
//                    // ex: inner duplicated cnt per gene (ex : ERBB2_2, ESR_3)
//                    new_line.append(type_name_and_inner_duplicated_map.get(type_name)[0]);
//                    new_line.append("\t");
//                    new_line.append(type_name);
//                    new_line.append("\t");
//                    new_line.append(type_num);
//                    new_line.append("\t");
//                    new_line.append(cnt_per_type);
//                    new_line.append("\t");
//                    new_line.append(type_cnt_devided_whole_cnt);
//                    new_line.append("\t");
//                    new_line.append(cnt_per_type / (double) count_patient_sum_of_specific_folder);
//                    new_line.append("\t");
//                    new_line.append(Double.valueOf(whole_prob_val) * type_cnt_devided_whole_cnt);
//                    new_line.append("\t");
//                    new_line.append(sample_id);
//
//                    // �������
//                    for (int i = 0; i < column_arr.length; i++) {
//                        new_line.append("\t");
//                        new_line.append(column_arr[i]);
//                    }
//                    list_to_return.add(new_line);
//                }
//            }
//
//        }
//        // System.out.println("list_to_return");
//        // System.out.println(list_to_return.toString());
//        return list_to_return;
//    }
//

}
