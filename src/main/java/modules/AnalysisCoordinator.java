package modules;

import main.DataPreprocessor;
import util.JwUtil;

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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class AnalysisCoordinator {

    public void make_mapped_sample_gene_list_tsv(DbManager dbm, boolean cna_included, String dataset_name) {
        // ** dataset_name : genie_4 / msk / summit / genie_6_1
        Statement st = dbm.connect_db();
        ResultSet rs = null;

        System.out.println(dataset_name);

        HashMap<String, String> sampleId_oncotree_map = new HashMap<>();
        HashMap<String, String> sampleId_cohort_map = new HashMap<>();
        HashMap<String, Set<String>> mutation_cna_merged_map = new HashMap<>();
        // queries
        String q_to_mapped_sample_cohort = "select sample_id, oncotree_codes, cancer_type from clinical_sample_tb order by sample_id";
        String q_to_cna_tb = "select distinct sample_id, hgnc_symbol, cna_value from cna_tb";
        String q_to_selected_mutation_mutsig_tb = "SELECT DISTINCT sample_id, hgnc_symbol FROM mutation_tb ORDER BY sample_id, hgnc_symbol";

        String sample_id = null, gene = null, oncotree = null, cohort = null, gene_cna = null;

        int cna_value = 0;
        BufferedWriter bw = null;

        String result_path_str = DataPreprocessor.TARGET_PROJECT_RESULT_PATH + "/" + DataPreprocessor.time_log + "_mapped";
        File result_path = new File(result_path_str);

        try {
            // make dir for output file
            result_path.mkdirs();

            // make file
            if (cna_included) {
                bw = new BufferedWriter(new FileWriter(result_path_str + "/sample_gene_list_cna_" + DataPreprocessor.time_log + ".tsv"));
            } else {
                bw = new BufferedWriter(new FileWriter(result_path_str + "/sample_gene_list_" + DataPreprocessor.time_log + ".tsv"));

            }
            // get sample_id - cohort map from db to data
            rs = st.executeQuery(q_to_mapped_sample_cohort);
            while (rs.next()) {
                sampleId_oncotree_map.put(rs.getString(1).trim(), rs.getString(2));
                sampleId_cohort_map.put(rs.getString(1).trim(), rs.getString(3));
            }
            rs.close();

            rs = st.executeQuery(q_to_selected_mutation_mutsig_tb);

            while (rs.next()) {
                sample_id = rs.getString(1).trim();
                System.out.println(sample_id);
                gene = rs.getString(2);
                if (!mutation_cna_merged_map.containsKey(sample_id)) {
                    mutation_cna_merged_map.put(sample_id, new HashSet<String>());
                }
                mutation_cna_merged_map.get(sample_id).add(gene);
            }
            rs.close();

            // cna add
            if (cna_included) {
                rs = st.executeQuery(q_to_cna_tb);
                while (rs.next()) {
                    sample_id = rs.getString(1).trim();
                    gene = rs.getString(2);
                    cna_value = rs.getInt(3);

                    if (cna_value == 2) {
                        gene_cna = gene + "_AMP";
                    } else if (cna_value == -2) {
                        gene_cna = gene + "_DEL";
                    } else {
                        gene_cna = "null";
                    }
                    if (!mutation_cna_merged_map.containsKey(sample_id)) {
                        mutation_cna_merged_map.put(sample_id, new HashSet<String>());
                    }
                    mutation_cna_merged_map.get(sample_id).add(gene_cna);
                }
            }


            // file write - tsv file
            for (String key_id : mutation_cna_merged_map.keySet()) {
                sample_id = key_id.trim();
                bw.write(sample_id);
                bw.write("\t");
                oncotree = sampleId_oncotree_map.get(sample_id);
                bw.write(oncotree);
                cohort = sampleId_cohort_map.get(sample_id);
                bw.flush();
                for (String item : mutation_cna_merged_map.get(sample_id)) {
                    bw.write("\t");
                    bw.write(item);
                    bw.flush();
                }
                bw.write("\n");
            }
            rs.close();
            bw.close();
            st.close();
        } catch (SQLException | IOException e) {
            result_path.delete();
            System.out.println(result_path.getPath());
            e.printStackTrace();
        }
    }

    public void sample_list_to_idx_list(String fileName_tsv, String dataset_name) {
        String cohort, line;
        String[] splited = null;
        StringBuilder sb = null, sb_with_sample_id = null;

        ArrayList<String> item_list = new ArrayList<>();
        HashMap<String, BufferedWriter> cohort_writer_map = new HashMap<>();
        HashMap<String, Integer> cohort_count_map = new HashMap<>();

        BufferedReader br = null;
        BufferedWriter bw, bw_all_cohort = null, bw_all_cohort_with_sample_id = null;
        int item_idx = 0, splited_size = 0;

        String result_path_str = DataPreprocessor.TARGET_PROJECT_RESULT_PATH + "/" + DataPreprocessor.time_log + "_indexed";
        File result_path = new File(result_path_str);

        try {
            // make dir
            result_path.mkdirs();

            br = new BufferedReader(new FileReader(fileName_tsv));
            bw_all_cohort = new BufferedWriter(new FileWriter(
                    result_path_str + "/" + DataPreprocessor.time_log + "_all_cohort" + ".tsv"));
            bw_all_cohort_with_sample_id = new BufferedWriter(new FileWriter(result_path_str + "/" + DataPreprocessor.time_log + "_all_cohort_with_sample_id" + ".tsv"));

            // file write (item index list by cohort)
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                cohort = splited[1];

                // cohort count
                if (cohort_count_map.containsKey(cohort)) {
                    cohort_count_map.replace(cohort, cohort_count_map.get(cohort) + 1);
                } else {
                    cohort_count_map.put(cohort, 1);
                }

                // replace "/" -> "&"
                if (cohort.contains("/")) {
                    cohort = cohort.replaceAll("/", "-");
                } else if (cohort.contains(" ")) {
                    cohort = cohort.replaceAll(" ", "");
                }
                splited_size = splited.length;

                sb = new StringBuilder();
                sb_with_sample_id = new StringBuilder();

                // *** if you want sample_id added
                sb_with_sample_id.append(splited[0] + "\t");

                // *** if you want cohort added
                sb_with_sample_id.append(splited[1] + "\t");

                for (int i = 2; i < splited_size; i++) {
                    if (!item_list.contains(splited[i])) {
                        item_list.add(splited[i]);
                    }
                    item_idx = item_list.indexOf(splited[i]);
                    if (i > 2) {
                        sb.append("\t");
                        sb_with_sample_id.append("\t");
                    }
                    sb.append(item_idx);
                    sb_with_sample_id.append(item_idx);
                }
                sb.append("\n");
                sb_with_sample_id.append("\n");

                if (!cohort_writer_map.containsKey(cohort)) {
                    cohort_writer_map.put(cohort, new BufferedWriter(new FileWriter(new File(result_path_str + "/" + cohort + "_" + DataPreprocessor.time_log + ".tsv"))));
                }
                bw = cohort_writer_map.get(cohort);
                bw.write(sb.toString());
                bw.flush();

                bw_all_cohort.write(sb.toString());
                bw_all_cohort.flush();

                bw_all_cohort_with_sample_id.write(sb_with_sample_id.toString());
                bw_all_cohort_with_sample_id.flush();

            }
            br.close();

            // file write (item - idx mapping file)
            bw = new BufferedWriter(new FileWriter(
                    result_path_str + "/" + DataPreprocessor.time_log + "_item-idx_mapping_list.tsv"));

            for (int i = 0; i < item_list.size(); i++) {
                bw.write(i + "\t" + item_list.get(i) + "\n");
                bw.flush();
            }
            bw.close();

            // file write (cohort count file)
            bw = new BufferedWriter(new FileWriter(
                    result_path_str + "/" + DataPreprocessor.time_log + "_cohort_count.tsv"));

            bw.write("cohort\tcount\n");
            for (String key_cohort : cohort_count_map.keySet()) {
                bw.write(key_cohort + "\t" + cohort_count_map.get(key_cohort) + "\n");
                bw.flush();
            }

            bw.close();
            bw_all_cohort.close();
            bw_all_cohort_with_sample_id.close();
        } catch (IOException e) {
            result_path.delete();
            e.printStackTrace();
        }
    }

    public void indexs_to_item_and_support_value(String folder_path, String source_time_log) {
        String idx_item_map_file = folder_path + "/" + source_time_log + "_item-idx_mapping_list.tsv";
        System.out.println(idx_item_map_file);

        HashMap<String, String> item_and_idx_map = new HashMap<>();
        HashMap<String, String> idx_and_item_map = new HashMap<>();
        load_item_and_idx_map(idx_item_map_file, item_and_idx_map, idx_and_item_map);

        File[] files_from_folder = new File(folder_path).listFiles();
        File comb_output_file = new File(folder_path + "/" + DataPreprocessor.TARGET_PROJECT_NAME + "_comb.out");
        String line, file_name, support_value = null, item = null;
        String[] splited = null;
        HashMap<String, String> oncotree_type_patient_cnt_map = new HashMap<>();
        double real_cnt = 0.0, patient_cnt_per_oncotree_code = 0.0;
        BufferedReader br = null;
        BufferedWriter bw = null;

        if (comb_output_file.exists()) {
            try {
                br = new BufferedReader(new FileReader(folder_path + "/" + source_time_log + "_cohort_count.tsv"));
                br.readLine();
                while ((line = br.readLine()) != null) {
                    splited = line.split("\t");
                    oncotree_type_patient_cnt_map.put(splited[0], splited[1]);
                }
                System.out.println(oncotree_type_patient_cnt_map);

                br = new BufferedReader(new FileReader(comb_output_file));
                file_name = comb_output_file.getName().split("\\_")[0];
                System.out.println(file_name);
                patient_cnt_per_oncotree_code = (double) Integer
                        .valueOf(oncotree_type_patient_cnt_map.get(file_name));
//
//                System.out.println(patient_cnt_per_oncotree_code);
//                bw = new BufferedWriter(new FileWriter(folder_path + "\\" + file_name + "_" + time_log + ".tsv"));
//
//                while ((line = br.readLine()) != null) {
//                    support_value = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
//                    real_cnt = Math.round(Double.valueOf(support_value) * patient_cnt_per_oncotree_code);
//
//                    if (real_cnt >= 3.00) {
//                        splited = line.split("\\(");
//                        splited = splited[0].split(" ");
//                        Arrays.sort(splited);
//
//                        for (int i = 0; i < splited.length; i++) {
//                            item = idx_and_item_map.get(splited[i]);
//                            bw.write(item);
//                            if (i != splited.length - 1) {
//                                bw.write(",");
//                            }
//                        }
//                        bw.write("\t");
//                        bw.write(support_value);
//                        bw.write("\t");
//                        real_cnt = Math.round(Double.valueOf(support_value) * patient_cnt_per_oncotree_code);
//                        bw.write(String.valueOf(real_cnt));
//                        bw.write("\n");
//                        bw.flush();
//                    }
//                }
//                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Done!");
        } else {
            System.out.println("ERROR : Combination Extracted File doesn't exist.");
        }
    }

    public static void load_item_and_idx_map(String
                                                     idx_item_map_file, HashMap<String, String> item_and_idx_map, HashMap<String, String> idx_and_item_map) {
        BufferedReader br = null;
        String line = null;
        String[] splited = null;
        try {
            br = new BufferedReader(new FileReader(idx_item_map_file));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                item_and_idx_map.put(splited[1].trim(), splited[0].trim());
                idx_and_item_map.put(splited[0].trim(), splited[1].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /// * 경계선 * ///


    public void aggregate_itemset_file(File[] fileList, String dataset_name) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String time_log = JwUtil.get_time_log();
        String cohort, line, support, key;
        String[] splited = null, idx_list = null;
        HashMap<String, StringBuilder> arr_cohort_support_map = new HashMap<>();

        for (File file : fileList) {
            cohort = file.getName().split("\\.")[0];
            try {
                br = new BufferedReader(new FileReader(file));
                bw = new BufferedWriter(
                        new FileWriter("C:\\" + dataset_name + "\\output\\aggreagted_itemset_" + time_log + ".tsv"));
                while ((line = br.readLine()) != null) {
                    splited = line.split("\\(");
                    splited[0] = splited[0].trim();
                    support = splited[1].split("\\)")[0];

                    if (!splited[0].contains(" ")) {
                        key = splited[0];
                        idx_list = new String[]{splited[0]};
                    } else {
                        idx_list = splited[0].split(" ");
                        // Arrays.sort(idx_list);
                    }

                    Arrays.sort(idx_list);
                    key = Arrays.toString(idx_list);
                    key = JwUtil.bracket_eliminater(key);
                    key = JwUtil.space_eliminater(key);

                    if (!arr_cohort_support_map.containsKey(key)) {
                        arr_cohort_support_map.put(key, new StringBuilder(cohort + ":" + support));
                    } else {
                        arr_cohort_support_map.get(key).append("," + cohort + ":" + support);
                    }
                }

                for (String k : arr_cohort_support_map.keySet()) {
                    // System.out.println(k + "\t" + arr_cohort_support_map.get(k));
                    bw.write(k + "\t" + arr_cohort_support_map.get(k));
                    bw.write("\n");
                    bw.flush();
                }
                br.close();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void make_count_vector_detailed(String itemset_file, String item_idx_mapping_file, String
            sample_itemlist,
                                           String sample_id_type_file, String dataset_name) {
        // dbo variable initiailze
        // String query_to_mapped_sample_type = "select sample_id,
        // sample_type,sample_type_detailed from mapped_sample_type order by sample_id";
        BufferedReader br = null;
        BufferedWriter bw = null, bw_sample_type_detailed = null, bw_sample_type = null;
        String line = null, idx_set, cohort, sample_id = null, sample_type_detailed = null, sample_type = null;
        String time_log = JwUtil.get_time_log();
        StringBuilder items = null;
        String[] idx_arr = null, splited = null, temp_items_arr;

        // HashMaps
        HashMap<String, String> item_idx_map = new HashMap<>();
        HashMap<String, String[]> sample_itemlist_map = new HashMap<>();
        HashMap<String, String> sample_cohort_map = new HashMap<>();
        HashMap<String, String> sample_sample_type_detailed_map = new HashMap<>();
        HashMap<String, String> sample_sample_type_map = new HashMap<>();
        // SortedSets
        SortedSet<String> cohort_set = new TreeSet<>();
        SortedSet<String> sample_type_detailed_set = new TreeSet<>();
        SortedSet<String> sample_type_set = new TreeSet<>();
        // ArrayLists
        ArrayList<String> cohort_list = new ArrayList<>();
        ArrayList<String> sample_type_detailed_list = new ArrayList<>();
        ArrayList<String> sample_type_list = new ArrayList<>();
        ArrayList<int[]> sample_type_detailed_by_cohort = new ArrayList<>();
        ArrayList<int[]> sample_type_by_cohort = new ArrayList<>();

        int[] temp_count_arr = null;
        int cohort_count = 0, cohort_list_idx, sample_type_detailed_list_size = 0, sample_type_detailed_list_idx = 0,
                sample_type_size = 0, sample_type_list_idx = 0;
        boolean contains = false;

        try {
            // ** sample_id - sample_type_detailted(in file) to variable
            br = new BufferedReader(new FileReader(sample_id_type_file));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");

                sample_sample_type_detailed_map.put(splited[0], splited[2]);
                sample_sample_type_map.put(splited[0], splited[1]);
                sample_type_detailed_set.add(splited[2]);
                sample_type_set.add(splited[1]);
            }

            br.close();

            // set to list - distinct sample_type_detailed
            sample_type_detailed_list.addAll(sample_type_detailed_set);
            sample_type_list.addAll(sample_type_set);

            // *** create file
            bw = new BufferedWriter(new FileWriter("C:\\" + dataset_name + "\\output\\count_vec_" + time_log + ".tsv"));
            bw_sample_type_detailed = new BufferedWriter(
                    new FileWriter("C:\\" + dataset_name + "\\output\\sample_type_detailed_vec_" + time_log + ".tsv"));
            bw_sample_type = new BufferedWriter(
                    new FileWriter("C:\\" + dataset_name + "\\output\\sample_type_vec_" + time_log + ".tsv"));

            // *** file read (item-idx mapping file to HashMap)
            br = new BufferedReader(new FileReader(item_idx_mapping_file));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                item_idx_map.put(splited[0].trim(), splited[1].trim());
            } // inner while 1 end
            br.close();

            // *** file read (sample-item list)
            br = new BufferedReader(new FileReader(sample_itemlist));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                sample_id = splited[0];
                cohort = splited[1];
                cohort_set.add(cohort);

                sample_cohort_map.put(sample_id, cohort);
                sample_itemlist_map.put(sample_id, Arrays.copyOfRange(splited, 2, splited.length));
            } // inner while 2 end

            br.close();

            // ** get cohort count , fill cohort_list, make temp arr
            cohort_count = cohort_set.size();
            sample_type_detailed_list_size = sample_type_detailed_list.size();
            sample_type_size = sample_type_list.size();

            cohort_list.addAll(cohort_set);
            // System.out.println(cohort_list);
            temp_count_arr = new int[cohort_count];

            // ** initialize - sample_type_detailed_by_cohort
            for (int i = 0; i < cohort_count; i++) {
                sample_type_detailed_by_cohort.add(new int[sample_type_detailed_list_size]);
                sample_type_by_cohort.add(new int[sample_type_size]);
            }

            // *** file read (itemset_file) & counting
            br = new BufferedReader(new FileReader(itemset_file));

            // ** bw first line
            bw.write(".");
            for (String cht : cohort_list) {
                bw.write("\t" + cht);
                bw_sample_type_detailed.write("\t" + cht);
                bw_sample_type.write("\t" + cht);
            }

            bw_sample_type.write("\n");
            bw_sample_type.flush();

            bw_sample_type_detailed.write("\n");
            bw_sample_type_detailed.flush();

            bw.write("\n");
            bw.flush();

            while ((line = br.readLine()) != null) {
                idx_set = line.split("\t")[0];
                idx_arr = idx_set.split(",");

                // *** temp_count_arr - redefine values 0
                for (int i = 0; i < cohort_count; i++) {
                    temp_count_arr[i] = 0;
                    // redefine - values 0 of sample_type_detailed_by_cohort
                    for (int s = 0; s < sample_type_detailed_list_size; s++) {
                        sample_type_detailed_by_cohort.get(i)[s] = 0;
                    }

                    for (int t = 0; t < sample_type_size; t++) {
                        sample_type_by_cohort.get(i)[t] = 0;
                    }
                }

                // print test - sample_type_detailed_by_cohort
                // for (int i = 0; i < cohort_count; i++) {
                // System.out.println(Arrays.toString(sample_type_detailed_by_cohort.get(i)));
                // }

                for (String s_id : sample_itemlist_map.keySet()) {
                    sample_id = s_id;
                    temp_items_arr = sample_itemlist_map.get(s_id);
                    // *** check contains
                    if (temp_items_arr.length >= idx_arr.length) {
                        if (JwUtil.arr_contains_other_arr(temp_items_arr, idx_arr)) {
                            contains = true;
                        }
                    }
                    if (contains) {
                        // ** cohort count add
                        // System.out.println("-------------contains-----------");
                        // System.out.println(Arrays.toString(idx_arr));
                        // System.out.println(Arrays.toString(temp_items_arr));
                        cohort = sample_cohort_map.get(s_id);
                        // System.out.println(s_id + "/" + cohort);
                        cohort_list_idx = cohort_list.indexOf(cohort);
                        temp_count_arr[cohort_list_idx]++;

                        // ** sample_type_detailed count add
                        sample_type_detailed = sample_sample_type_detailed_map.get(s_id);
                        sample_type_detailed_list_idx = sample_type_detailed_list.indexOf(sample_type_detailed);
                        sample_type_detailed_by_cohort.get(cohort_list_idx)[sample_type_detailed_list_idx]++;

                        // ** sample_type count add
                        sample_type = sample_sample_type_map.get(s_id);
                        sample_type_list_idx = sample_type_list.indexOf(sample_type);
                        sample_type_by_cohort.get(cohort_list_idx)[sample_type_list_idx]++;
                    }
                    contains = false;
                } // for loop end

                // *** file write(count file)
                items = new StringBuilder();
                for (int i = 0; i < idx_arr.length; i++) {
                    if (i != 0) {
                        items.append(",");
                    }
                    items.append(item_idx_map.get(idx_arr[i]));
                }

                bw.write(items.toString());
                bw_sample_type.write(items.toString());
                bw_sample_type_detailed.write(items.toString());

                for (int i = 0; i < cohort_count; i++) {
                    bw.write("\t" + temp_count_arr[i]);
                    bw_sample_type_detailed.write("\t");
                    bw_sample_type.write("\t");
                    for (int s = 0; s < sample_type_detailed_list_size; s++) {
                        if (s > 0) {
                            bw_sample_type_detailed.write(',');
                        }
                        bw_sample_type_detailed.write(String.valueOf(sample_type_detailed_by_cohort.get(i)[s]));
                    }

                    for (int t = 0; t < sample_type_size; t++) {
                        if (t > 0) {
                            bw_sample_type.write(',');
                        }
                        bw_sample_type.write(String.valueOf(sample_type_by_cohort.get(i)[t]));
                    }
                }

                bw_sample_type_detailed.write("\n");
                bw_sample_type_detailed.flush();

                bw_sample_type.write("\n");
                bw_sample_type.flush();

                bw.write("\n");
                bw.flush();

                // System.exit(0);

            } // inner while 3 end
            System.out.println("finish time : " + JwUtil.get_time_log());
            bw_sample_type_detailed.close();
            bw_sample_type.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void make_count_vector(String itemset_file, String item_idx_mapping_file, String sample_itemlist,
                                  DbManager dbo, String dataset_name) {
        // dbo variable initiailze
        Statement st = null;
        ResultSet rs = null;
        String query_to_mapped_sample_type = "select sample_id, sample_type, sample_type_detailed from mapped_sample_type_tb order by sample_id";
        BufferedReader br = null;
        BufferedWriter bw = null, bw_sample_type_detailed = null, bw_sample_type = null;
        String line = null, idx_set, cohort, sample_id = null, sample_type_detailed = null, sample_type = null;
        String time_log = JwUtil.get_time_log();
        StringBuilder items = null;
        String[] idx_arr = null, splited = null, temp_items_arr;

        // HashMaps
        HashMap<String, String> item_idx_map = new HashMap<>();
        HashMap<String, String[]> sample_itemlist_map = new HashMap<>();
        HashMap<String, String> sample_cohort_map = new HashMap<>();
        HashMap<String, String> sample_sample_type_detailed_map = new HashMap<>();
        HashMap<String, String> sample_sample_type_map = new HashMap<>();
        // SortedSets
        SortedSet<String> cohort_set = new TreeSet<>();
        SortedSet<String> sample_type_detailed_set = new TreeSet<>();
        SortedSet<String> sample_type_set = new TreeSet<>();
        // ArrayLists
        ArrayList<String> cohort_list = new ArrayList<>();
        ArrayList<String> sample_type_detailed_list = new ArrayList<>();
        ArrayList<String> sample_type_list = new ArrayList<>();
        ArrayList<int[]> sample_type_detailed_by_cohort = new ArrayList<>();
        ArrayList<int[]> sample_type_by_cohort = new ArrayList<>();

        int[] temp_count_arr = null;
        int cohort_count = 0, cohort_list_idx, sample_type_detailed_list_size = 0, sample_type_detailed_list_idx = 0,
                sample_type_size = 0, sample_type_list_idx = 0;
        boolean contains = false;

        try {
            // ** sample_id - sample_type_detailted(in db) to variable
            st = dbo.connect_db();
            rs = st.executeQuery(query_to_mapped_sample_type);
            while (rs.next()) {
                sample_sample_type_detailed_map.put(rs.getString(1), rs.getString(3));
                sample_sample_type_map.put(rs.getString(1), rs.getString(2));
                sample_type_detailed_set.add(rs.getString(3));
                sample_type_set.add(rs.getString(2));
            }

            // set to list - distinct sample_type_detailed
            sample_type_detailed_list.addAll(sample_type_detailed_set);
            sample_type_list.addAll(sample_type_set);

            // *** create file
            bw = new BufferedWriter(new FileWriter("C:\\" + dataset_name + "\\output\\count_vec_" + time_log + ".tsv"));
            bw_sample_type_detailed = new BufferedWriter(
                    new FileWriter("C:\\" + dataset_name + "\\output\\sample_type_detailed_vec_" + time_log + ".tsv"));
            bw_sample_type = new BufferedWriter(
                    new FileWriter("C:\\" + dataset_name + "\\output\\sample_type_vec_" + time_log + ".tsv"));

            // *** file read (item-idx mapping file to HashMap)
            br = new BufferedReader(new FileReader(item_idx_mapping_file));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                item_idx_map.put(splited[0].trim(), splited[1].trim());
            } // inner while 1 end
            br.close();

            // *** file read (sample-item list)
            br = new BufferedReader(new FileReader(sample_itemlist));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                sample_id = splited[0];
                cohort = splited[1];
                cohort_set.add(cohort);

                sample_cohort_map.put(sample_id, cohort);
                sample_itemlist_map.put(sample_id, Arrays.copyOfRange(splited, 2, splited.length));
            } // inner while 2 end

            br.close();

            // ** get cohort count , fill cohort_list, make temp arr
            cohort_count = cohort_set.size();
            sample_type_detailed_list_size = sample_type_detailed_list.size();
            sample_type_size = sample_type_list.size();

            cohort_list.addAll(cohort_set);
            // System.out.println(cohort_list);
            temp_count_arr = new int[cohort_count];

            // ** initialize - sample_type_detailed_by_cohort
            for (int i = 0; i < cohort_count; i++) {
                sample_type_detailed_by_cohort.add(new int[sample_type_detailed_list_size]);
                sample_type_by_cohort.add(new int[sample_type_size]);
            }

            // *** file read (itemset_file) & counting
            br = new BufferedReader(new FileReader(itemset_file));

            // ** bw first line
            bw.write(".");
            for (String cht : cohort_list) {
                bw.write("\t" + cht);
                bw_sample_type_detailed.write("\t" + cht);
                bw_sample_type.write("\t" + cht);
            }
            bw_sample_type.write("\n");
            bw_sample_type.flush();

            bw_sample_type_detailed.write("\n");
            bw_sample_type_detailed.flush();

            bw.write("\n");
            bw.flush();

            while ((line = br.readLine()) != null) {
                idx_set = line.split("\t")[0];
                idx_arr = idx_set.split(",");

                // *** temp_count_arr - redefine values 0
                for (int i = 0; i < cohort_count; i++) {
                    temp_count_arr[i] = 0;
                    // redefine - values 0 of sample_type_detailed_by_cohort
                    for (int s = 0; s < sample_type_detailed_list_size; s++) {
                        sample_type_detailed_by_cohort.get(i)[s] = 0;
                    }

                    for (int t = 0; t < sample_type_size; t++) {
                        sample_type_by_cohort.get(i)[t] = 0;
                    }
                }

                // print test - sample_type_detailed_by_cohort
                // for (int i = 0; i < cohort_count; i++) {
                // System.out.println(Arrays.toString(sample_type_detailed_by_cohort.get(i)));
                // }

                for (String s_id : sample_itemlist_map.keySet()) {
                    sample_id = s_id;
                    temp_items_arr = sample_itemlist_map.get(s_id);
                    // *** check contains
                    if (temp_items_arr.length >= idx_arr.length) {
                        if (JwUtil.arr_contains_other_arr(temp_items_arr, idx_arr)) {
                            contains = true;
                        }
                    }
                    if (contains) {
                        // ** cohort count add
                        // System.out.println("-------------contains-----------");
                        // System.out.println(Arrays.toString(idx_arr));
                        // System.out.println(Arrays.toString(temp_items_arr));
                        cohort = sample_cohort_map.get(s_id);
                        // System.out.println(s_id + "/" + cohort);
                        cohort_list_idx = cohort_list.indexOf(cohort);
                        temp_count_arr[cohort_list_idx]++;

                        // ** sample_type_detailed count add
                        sample_type_detailed = sample_sample_type_detailed_map.get(s_id);
                        sample_type_detailed_list_idx = sample_type_detailed_list.indexOf(sample_type_detailed);
                        sample_type_detailed_by_cohort.get(cohort_list_idx)[sample_type_detailed_list_idx]++;

                        // ** sample_type count add
                        sample_type = sample_sample_type_map.get(s_id);
                        sample_type_list_idx = sample_type_list.indexOf(sample_type);
                        sample_type_by_cohort.get(cohort_list_idx)[sample_type_list_idx]++;
                    }
                    contains = false;
                } // for loop end

                // *** file write(count file)
                items = new StringBuilder();
                for (int i = 0; i < idx_arr.length; i++) {
                    if (i != 0) {
                        items.append(",");
                    }
                    items.append(item_idx_map.get(idx_arr[i]));
                }
                bw.write(items.toString());
                bw_sample_type.write(items.toString());
                bw_sample_type_detailed.write(items.toString());

                for (int i = 0; i < cohort_count; i++) {
                    bw.write("\t" + temp_count_arr[i]);
                    bw_sample_type_detailed.write("\t");
                    bw_sample_type.write("\t");
                    for (int s = 0; s < sample_type_detailed_list_size; s++) {
                        if (s > 0) {
                            bw_sample_type_detailed.write(',');
                        }
                        bw_sample_type_detailed.write(String.valueOf(sample_type_detailed_by_cohort.get(i)[s]));
                    }

                    for (int t = 0; t < sample_type_size; t++) {
                        if (t > 0) {
                            bw_sample_type.write(',');
                        }
                        bw_sample_type.write(String.valueOf(sample_type_by_cohort.get(i)[t]));
                    }
                }

                bw_sample_type_detailed.write("\n");
                bw_sample_type_detailed.flush();

                bw_sample_type.write("\n");
                bw_sample_type.flush();

                bw.write("\n");
                bw.flush();

                // System.exit(0);

            } // inner while 3 end
            System.out.println("finish time : " + JwUtil.get_time_log());
            bw_sample_type_detailed.close();
            bw.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void count_vec_to_support_vec(String count_file, String cohort_count_file) {
        BufferedReader br = null;
        BufferedWriter bw_divided = null, bw_support = null;
        HashMap<String, Integer> cohort_count_map = new HashMap<>();
        ArrayList<String> cohort_order_list = new ArrayList<>();
        ArrayList<Integer> cohort_count_list = new ArrayList<>();
        ArrayList<Double> divided_val_list = new ArrayList<>();
        String line = null;
        String time_log = JwUtil.get_time_log();
        String[] splited = null;
        int cnt_val = 0;
        double divided_val = 0.0, divided_sum = 0.0;

        try {
            // *** count file
            br = new BufferedReader(new FileReader(cohort_count_file));
            br.readLine();
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                cohort_count_map.put(splited[0], Integer.valueOf(splited[1]));
            }
            br.close();

            // *** count vec -> support vec
            br = new BufferedReader(new FileReader(count_file));
            bw_divided = new BufferedWriter(new FileWriter("C:\\genie_4\\output\\support_vec_" + time_log + ".tsv"));
            bw_support = new BufferedWriter(new FileWriter("C:\\genie_4\\output\\prob_vec_" + time_log + ".tsv"));

            // first line
            line = br.readLine();
            splited = line.split("\t");
            bw_divided.write(".");
            for (int i = 1; i < splited.length; i++) {
                cohort_order_list.add(splited[i]);
                cohort_count_list.add(cohort_count_map.get(splited[i]));
                bw_divided.write("\t" + splited[i]);
                bw_support.write("\t" + splited[i]);
            }
            bw_divided.write("\n");
            bw_divided.flush();

            bw_support.write("\n");
            bw_support.flush();

            // System.out.println(cohort_order_list);
            // System.out.println(cohort_count_list);

            while ((line = br.readLine()) != null) {
                divided_sum = 0.0;
                divided_val_list.clear();
                splited = line.split("\t");
                bw_divided.write(splited[0]);
                bw_support.write(splited[0]);

                for (int i = 1; i < splited.length; i++) {
                    cnt_val = Integer.valueOf(splited[i]);
                    divided_val = cnt_val / (double) cohort_count_list.get(i - 1);
                    // System.out.println(cnt_val + "/" + cohort_count_list.get(i - 1));
                    // System.out.println(divided_val);
                    divided_sum += divided_val;
                    bw_divided.write("\t" + divided_val);
                    divided_val_list.add(divided_val);
                }
                bw_divided.write("\n");
                bw_divided.flush();

                // ** file write (support vec)
                // System.out.println(divided_val_list);
                // System.out.println(divided_sum);
                // write support val
                for (double d_val : divided_val_list) {
                    bw_support.write("\t" + d_val / divided_sum);
                }
                bw_support.write("\n");
                bw_support.flush();

            }
            br.close();
            bw_divided.close();
            bw_support.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void count_items_by_sample(String file_name, String dataset_name) {
        // for hypermutated elimination
        String line = null, new_file = null;
        String time_log = JwUtil.get_time_log();
        String[] splited = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        new_file = file_name.split("\\\\")[2];
        new_file = new_file.split("\\.")[0];
        // System.out.println(new_file);
        int cna_cnt = 0;

        try {
            br = new BufferedReader(new FileReader(file_name));
            bw = new BufferedWriter(
                    new FileWriter("C:\\" + dataset_name + "\\" + new_file + "_only cnt_" + time_log + ".tsv"));
            bw.write("sample_id\tcohort\tmutation cnt\tcna_cnt\tcnt_sum\n");
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                bw.write(splited[0]);
                bw.write("\t" + splited[1]);

                for (String item : splited) {
                    if (item.contains("_AMP") || item.contains("_DEL")) {
                        cna_cnt++;
                    }
                }

                bw.write("\t" + ((splited.length) - 2 - cna_cnt));
                bw.write("\t" + cna_cnt);
                bw.write("\t" + (splited.length - 2));

                cna_cnt = 0;
                bw.write("\n");
                bw.flush();
            }
            bw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cutoff_hypermutated_sample(String sample_item_list_filleName, String cutoff_fileName,
                                           String dataset_name) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String line = null;
        String[] splited = null;
        HashSet<String> cutoff_samples_set = new HashSet<>();
        splited = sample_item_list_filleName.split("\\\\");
        String new_file_name = splited[splited.length - 1];
        new_file_name = new_file_name.split("\\.")[0];
        String time_log = JwUtil.get_time_log();

        try {
            // step 1. put cutoff sample ids in set
            br = new BufferedReader(new FileReader(cutoff_fileName));
            while ((line = br.readLine()) != null) {
                cutoff_samples_set.add(line.trim());
            }
            br.close();

            // step 2.
            br = new BufferedReader(new FileReader(sample_item_list_filleName));
            bw = new BufferedWriter(
                    new FileWriter("C:\\" + dataset_name + "\\" + new_file_name + "_cutoff(" + time_log + ").tsv"));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                if (!cutoff_samples_set.contains(splited[0])) {
                    bw.write(line + "\n");
                }
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void idx_list_to_item(String idx_file, String item_idx_mapping_file, String cohort_name) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String line = null, support = null, key = null;
        StringBuilder item = null;
        String time_log = JwUtil.get_time_log();
        String[] splited = null, idx_list = null;
        HashMap<String, String> item_idx_map = new HashMap<>();
        try {
            // *** item-idx mapping file
            br = new BufferedReader(new FileReader(item_idx_mapping_file));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                item_idx_map.put(splited[0], splited[1]);
            }
            br.close();

            br = new BufferedReader(new FileReader(idx_file));
            bw = new BufferedWriter(
                    new FileWriter("C:\\summit\\output\\" + cohort_name + "_itemset_" + time_log + ".tsv"));
            bw.write("item_set\tsupport\n");
            while ((line = br.readLine()) != null) {
                splited = line.split("\\(");
                splited[0] = splited[0].trim();
                support = splited[1].split("\\)")[0];

                if (!splited[0].contains(" ")) {
                    key = splited[0];
                    item = new StringBuilder(item_idx_map.get(key));
                } else {
                    item = new StringBuilder("");
                    idx_list = splited[0].split(" ");
                    Arrays.sort(idx_list);
                    for (String idx : idx_list) {
                        item.append(item_idx_map.get(idx));
                        item.append(",");
                    }
                    item.delete(item.length() - 1, item.length());
                }

                bw.write(item.toString() + "\t" + support + "\n");
                bw.flush();
            }
            bw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void aggregated_itemset_to_vectors(String idx_item_mapping_file, String aggregate_item_set_file,
                                              String cohort_count_file, String full_cohort_name_file, String dataset_name) {
        BufferedReader br = null;
        BufferedWriter bw_cnt = null, bw_sup = null, bw_prob = null;
        String line, cohort = null, sup_val;
        String time_log = JwUtil.get_time_log();
        String file_name = aggregate_item_set_file.split("\\\\")[3].split("\\.")[0];
        String[] splited, idx_arr, val_arr, cohort_val_arr;
        Double[] sup_arr;
        double sup_val_double = 0.0, sup_val_double_sum = 0.0;
        StringBuilder items = null;
        int cohort_idx = 0, cohort_count = 0;

        // ArrayLists
        ArrayList<String> cohort_abbv_list = new ArrayList<>();
        ArrayList<String> cohort_full_list = new ArrayList<>();
        ArrayList<String> sup_arr_list = new ArrayList<>();

        // HashMaps
        HashMap<String, String> idx_item_map = new HashMap<>();
        HashMap<String, Integer> cohort_count_map = new HashMap<>();

        try {
            // ** make dir
            new File("C:\\" + dataset_name + "\\output\\" + time_log).mkdirs();

            // ** make bw file
            bw_cnt = new BufferedWriter(new FileWriter("C:\\" + dataset_name + "\\output\\" + time_log + "\\cnt_vec_"
                    + time_log + "[" + file_name + "].tsv"));
            bw_sup = new BufferedWriter(new FileWriter("C:\\" + dataset_name + "\\output\\" + time_log + "\\sup_vec_"
                    + time_log + "[" + file_name + "].tsv"));
            bw_prob = new BufferedWriter(new FileWriter("C:\\" + dataset_name + "\\output\\" + time_log + "\\prob_vec_"
                    + time_log + "[" + file_name + "].tsv"));

            // ** full_cohort_name_file
            br = new BufferedReader(new FileReader(full_cohort_name_file));
            br.readLine();
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                cohort_full_list.add(splited[0]);
                cohort_abbv_list.add(splited[1]);
            }
            br.close();

            // ** idx_item_mapping_file
            br = new BufferedReader(new FileReader(idx_item_mapping_file));
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                idx_item_map.put(splited[0].trim(), splited[1].trim());
            }
            br.close();

            // ** cohort_count_file
            br = new BufferedReader(new FileReader(cohort_count_file));
            br.readLine();
            while ((line = br.readLine()) != null) {
                splited = line.split("\t");
                cohort_count_map.put(splited[0], Integer.valueOf(splited[1]));
            }
            br.close();

            // write first line
            bw_sup.write(".");
            bw_cnt.write(".");
            bw_prob.write(".");
            for (String full_cohort_name : cohort_full_list) {
                bw_sup.write("\t");
                bw_sup.write(full_cohort_name);
                bw_cnt.write("\t");
                bw_cnt.write(full_cohort_name);
                bw_prob.write("\t");
                bw_prob.write(full_cohort_name);
            }
            bw_sup.write("\n");
            bw_sup.flush();
            bw_cnt.write("\n");
            bw_cnt.flush();
            bw_prob.write("\n");
            bw_prob.flush();

            // ** aggregate_item_set_file
            br = new BufferedReader(new FileReader(aggregate_item_set_file));
            while ((line = br.readLine()) != null) {
                for (int i = 0; i < cohort_abbv_list.size(); i++) {
                    sup_arr_list.add("0");
                }
                splited = line.split("\t");
                idx_arr = splited[0].split(",");
                val_arr = splited[1].split(",");

                // ** idx list to item name
                items = new StringBuilder();
                for (int i = 0; i < idx_arr.length; i++) {
                    if (i != 0) {
                        items.append(",");
                    }
                    items.append(idx_item_map.get(idx_arr[i]));
                }

                // ** write itemset name
                bw_cnt.write(items.toString());
                bw_sup.write(items.toString());
                bw_prob.write(items.toString());

                for (String val : val_arr) {
                    cohort_val_arr = val.split(":");
                    cohort_idx = cohort_abbv_list.indexOf(cohort_val_arr[0]);
                    sup_arr_list.set(cohort_idx, cohort_val_arr[1]);
                }

                sup_val_double_sum = 0.0;
                for (int i = 0; i < sup_arr_list.size(); i++) {
                    sup_val = sup_arr_list.get(i);
                    if (sup_val.equals("0")) {
                        bw_cnt.write("\t0");
                        bw_sup.write("\t0");
                    } else {
                        // sup
                        sup_val_double = Double.valueOf(sup_val);
                        sup_val_double_sum += sup_val_double;
                        bw_sup.write("\t" + sup_val);
                        // cnt
                        cohort = cohort_full_list.get(i);
                        cohort_count = cohort_count_map.get(cohort);
                        bw_cnt.write("\t" + Math.round(cohort_count * sup_val_double));
                    }
                }
                bw_sup.write("\n");
                bw_sup.flush();
                bw_cnt.write("\n");
                bw_cnt.flush();

                // prob vec
                for (int i = 0; i < sup_arr_list.size(); i++) {
                    sup_val = sup_arr_list.get(i);
                    if (sup_val.equals("0")) {
                        bw_prob.write("\t0");
                    } else {
                        sup_val_double = Double.valueOf(sup_val);
                        bw_prob.write("\t" + (sup_val_double / sup_val_double_sum));
                    }
                }
                bw_prob.write("\n");
                bw_prob.flush();

                // clear
                sup_arr_list.clear();
            }
            bw_sup.close();
            bw_cnt.close();
            bw_prob.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add_clinical_benefit_data_to_tsv(DbManager dbo, String tsv_file) {
        Statement st = dbo.connect_db();
        String sql = "select patient_id, response_rate, tumor_change, progress_free_survival, mutation_burden from clinical_patient_tb";
        ResultSet rs = null;
        String patient_id, rr, tc, pfs, mb, line;
        HashMap<String, String[]> map = new HashMap<>();
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            rs = st.executeQuery(sql);
            while (rs.next()) {
                patient_id = rs.getString(1);
                rr = rs.getString(2);
                tc = rs.getString(3);
                pfs = rs.getString(4);
                mb = rs.getString(5);
                map.put(patient_id, new String[]{rr, tc, pfs, mb});
            }

            br = new BufferedReader(new FileReader(tsv_file));
            while ((line = br.readLine()) != null) {

            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

    }

}
