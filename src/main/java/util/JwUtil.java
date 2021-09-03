package util;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;

public final class JwUtil {
    public static String get_time_log() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }

    public static String[] remove_comma_and_space(String str) {
        String[] arr = str.split(",");
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].trim();
        }
        return arr;
    }

    public static boolean arr_contains_other_arr(Object[] bigger_arr, Object[] smaller_arr) {
        Arrays.sort(bigger_arr);
        for (Object i : smaller_arr) {
            if (Arrays.binarySearch(bigger_arr, i) < 0) {
                return false;
            }
        }
        return true;
    }

    public static String get_only_gene_name(String gene_cna_name) {
        String rt_str = gene_cna_name.substring(0, gene_cna_name.lastIndexOf("_"));
        return rt_str;
    }

    public static int[] sum_two_arr_value(int[] first_arr, int[] second_arr) {
        int[] sum_arr = new int[first_arr.length];

        for (int i = 0; i < first_arr.length; i++) {
            sum_arr[i] = first_arr[i] + second_arr[i];
        }
        return sum_arr;
    }

    public static String bracket_eliminater(String word) {
        String r_word;
        r_word = word.replaceAll("\\[", "");
        r_word = r_word.replaceAll("\\]", "");
        r_word = r_word.trim();
        return r_word;
    }

    public static String quick_go_bracket_eliminater(String word) {
        String r_word;
        r_word = word.replaceAll("\\[", "");
        r_word = r_word.replaceAll("\\]", "");
        r_word = r_word.replaceAll("\\{", "");
        r_word = r_word.replaceAll("\\}", "");
        r_word = r_word.replaceAll("\\\"", "");
        r_word = r_word.replaceAll("connectedXrefs", "");
        r_word = r_word.replaceAll(",id:", ":");
        r_word = r_word.replaceAll(":db:", "");
        r_word = r_word.trim();
        return r_word;
    }

    public static String space_eliminater(String word) {
        String r_word;
        r_word = word.replaceAll(" ", "");
        return r_word;
    }

    public static String double_quote_eliminater(String word) {
        String r_word;
        r_word = word.replaceAll("\\\"", "");
        return r_word;
    }

    public static String escape_character_coverter(String str) {
        String rtr_str = null;
        Properties p = new Properties();
        try {
            p.load(new StringReader("key=" + str));
            rtr_str = p.getProperty("key");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rtr_str;
    }

    public static String slash_to_and(String str) {
        String rtr_str = null;
        if (str.contains("/")) {
            rtr_str = str.replaceAll("/", " or ");
        } else {
            rtr_str = str;
        }
        return rtr_str;
    }

    public static String comma_space_to_tab(String str) {
        String new_str = str.replaceAll(", ", "\t");
        return new_str;
    }

    public static double mean(double[] array) { // ��� ��� ���ϱ�
        double sum = 0.0;

        for (int i = 0; i < array.length; i++)
            sum += array[i];

        return sum / array.length;
    }

    public static double var_p(double[] array, int option) { // ��ü �������� ǥ������ ���ϱ�
        if (array.length < 2)
            return Double.NaN;

        double sum = 0.0;
        double return_val = 0.0;
        double diff;
        double meanValue = mean(array);

        for (int i = 0; i < array.length; i++) {
            diff = array[i] - meanValue;
            sum += diff * diff;
        }
        return_val = sum / (array.length - option);
        // return_val = Math.sqrt(sum / (array.length - option));

        return return_val;
    }

    public static double[] str_arr_to_dbl_arr(String[] arr) {
        double[] return_arr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            return_arr[i] = Double.valueOf(arr[i]);
        }
        return return_arr;
    }

    public static double get_vec_len(String[] s_arr) {
        double[] arr = str_arr_to_dbl_arr(s_arr);
        int arr_size = arr.length;
        double var_p_val = var_p(arr, 0);
        double vec_len = Math.sqrt(arr_size * var_p_val + (1.0 / arr_size));
        return vec_len;
    }

    public static double get_median_from_arr(Object[] arr) {
        double[] sorted_arr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            sorted_arr[i] = (double) arr[i];
        }

        double median = 0.0;
        if (sorted_arr.length % 2 == 0) {
            median = ((double) sorted_arr[sorted_arr.length / 2] + (double) sorted_arr[sorted_arr.length / 2 - 1]) / 2;
        } else {
            median = (double) sorted_arr[sorted_arr.length / 2];
        }
        return median;
    }

}
