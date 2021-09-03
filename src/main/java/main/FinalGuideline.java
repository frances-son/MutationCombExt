package main;

import modules.AnalysisCoordinator;
import modules.CombExtractor;
import util.JwUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FinalGuideline {
    public static void main(String[] args) {
        CombExtractor comb_extractor = new CombExtractor();


        // ---- 경계선 ---- //
//        OncokbReader okreader = new OncokbReader();
//		String tsv_file_by_cancer_type_folder_name = "C:\\genie_6_1\\output\\20190902_103203";
//		String indexed_file_folder_name = "C:\\genie_6_1\\output\\20190918_095922_indexed_files__under_100_deleted";
//        String project_name = "genie_7_0";
//		String one_cancer_type_only_file_name = "C:\\genie_6_1\\output\\20190902_103203\\Breast Cancer_sample_gene_list_cna_20190902_103203.tsv";
//        String combination_folder_path = "C:\\genie_7_0\\output\\20200603_BRCA_deep_analysis";
//        String splited_patient_list_folder_path = combination_folder_path + "\\patient_list";
//        int count_patient_sum_of_specific_folder = 0;

        // ** get time stamp for file name
//        String time_log = util.get_time_log();

        // **** FINAL GUIDELINE ****
        // ** STEP 1. make folder **

        // make directory : new clinical trial project
//        File new_clinical_trial_path = new File(TARGET_PROJECT_PATH);
//        if (!new_clinical_trial_path.exists()) {
//            new_clinical_trial_path.mkdirs();
//        }
//
//        File new_results_path = new File(TARGET_PROJECT_PATH + "/" + time_log);
//        // make directory : results folder named after time_log
//        if (!new_results_path.exists()) {
//            new_results_path.mkdirs();
//        }

//         ** STEP 4. idx to item and support value
//                        get_comb.indexs_to_item_and_support_value("C:\\summit_2020\\output\\");


//		 ** Before STEP 5. count patients
//		count_patient_sum_of_specific_folder = get_comb
//				.count_patients_in_specific_folder(splited_patient_list_folder_path);
//		System.out.println(count_patient_sum_of_specific_folder);

        // ** STEP 5. deep analysis
//		get_comb.get_hgvsp_short_and_variant_classification_by_combination(combination_folder_path,
//				splited_patient_list_folder_path, count_patient_sum_of_specific_folder);


    }
}