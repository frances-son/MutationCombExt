package main;

import modules.AnalysisCoordinator;
import modules.DbManager;
import util.JwUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DataPreprocessor {
    public static String TARGET_PROJECT_NAME = "alpelisib";
    public static String TARGET_PROJECT_SOURCE_PATH = "src/resources/" + TARGET_PROJECT_NAME;
    public static String TARGET_PROJECT_RESULT_PATH = "results/" + TARGET_PROJECT_NAME;
    public static String time_log = JwUtil.get_time_log();

    public static void main(String args[]) {
        String cna_file_path = TARGET_PROJECT_SOURCE_PATH + "/data_CNA.txt";
        DbManager dbm = new DbManager("jdbc:mysql://localhost/alpelisib?serverTimezone=UTC", "root", "1541");
        Statement st = dbm.connect_db();
        AnalysisCoordinator analysis = new AnalysisCoordinator();

        /* STEP 0 : make folder for PROJECT SOURCE, RESULT FILES */
        // make directory : new clinical trial project
        File new_clinical_trial_path = new File(TARGET_PROJECT_RESULT_PATH);
        if (!new_clinical_trial_path.exists()) {
            new_clinical_trial_path.mkdirs();
        }

        /* STEP 1 : fill 'cna_tb' table with original source text file */
//        dbm.fill_cna_tb(st, cna_file_path, "cna_tb");

        /* STEP 2 : db data -> mapped tsv file (sample_id - mutated gene, cna gene list) */
//        analysis.make_mapped_sample_gene_list_tsv(dbm, true, TARGET_PROJECT_NAME);

        /* STEP 3 : mapping mapped tsv file -> indexed file */
//        String step2_time_log = "20210831_162345";
//        String mapped_tsv_file_path = new File(TARGET_PROJECT_RESULT_PATH + "/" + step2_time_log + "_mapped").listFiles()[0].getAbsolutePath();
//        analysis.sample_list_to_idx_list(mapped_tsv_file_path, TARGET_PROJECT_NAME);

        /* STEP 4 : combination extracted result file (indexed) -> item mapping*/
        String step3_time_log = "20210901_144622";
        analysis.indexs_to_item_and_support_value(TARGET_PROJECT_RESULT_PATH+"/"+step3_time_log+"_indexed", step3_time_log);


    }
}
