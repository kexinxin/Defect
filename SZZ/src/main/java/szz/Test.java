package szz;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args){
        String[] cccHeader={"Number_of_modules","Lines_of_code","Line_of_code_per_module","McCabes_cyclomatic_complexity",
                "McCabes_cyclomatic_complexity_per_module","Lines_of_comment","Lines_of_comment_per_module","Lines_of_code_per_line_of_comment",
                "McCabes_cyclomatic_complexity_per_line_of_comment","IF4","IF4_per_module", "IF4_visible","IF4_visible_per_module","IF4_concrete"
                ,"IF4_concrete","rejected_lines_of_code"};
        String[] cqmetricsHeader={ "nchar","nline","ine_length_min", "line_length_mean","line_length_median", "line_length_max", "line_length_sd",
                "nempty_line","nfunction","nstatement","statement_nesting_min", "statement_nesting_mean", "statement_nesting_median","statement_nesting_max",
                "statement_nesting_sd","ninternal","nconst","nenum","ngoto","ninline","nnoalias","nregister","nrestrict","nsigned","nstruct","nunion",
                "nunsigned","nvoid","nvolatile","ntypedef","ncomment","ncomment_char","nboilerplate_comment_char","ndox_comment","ndox_comment_char",
                "nfun_comment","ncpp_directive","ncpp_include","ncpp_conditional","nfun_cpp_directive","nfun_cpp_conditional","style_inconsistency", "nfunction2",
                "halstead_min","halstead_mean","halstead_median","halstead_max","halstead_sd","nfunction3", "cyclomatic_min", "cyclomatic_mean", "cyclomatic_median",
                "cyclomatic_max","cyclomatic_sd","nidentifier","identifier_length_min","identifier_length_mean","identifier_length_median","identifier_length_max",
                "identifier_length_sd","unique_nidentifier","unique_identifier_length_min","unique_identifier_length_mean", "unique_identifier_length_median",
                "unique_identifier_length_max","unique_identifier_length_sd","indentation_spacing_count","indentation_spacing_min","indentation_spacing_mean",
                "indentation_spacing_median","indentation_spacing_max","indentation_spacing_sd","nno_space_after_binary_op","nno_space_after_closing_brace",
                "nno_space_after_comma","nno_space_after_keyword", "nno_space_after_opening_brace", "nno_space_after_semicolon","nno_space_before_binary_op",
                "nno_space_before_closing_brace", "nno_space_before_keyword", "nno_space_before_opening_brace", "nspace_after_opening_square_bracket",
                "nspace_after_struct_op","nspace_after_unary_op", "nspace_at_end_of_line", "nspace_before_closing_bracket","nspace_before_closing_square_bracket",
                "nspace_before_comma", "nspace_before_opening_square_bracket", "nspace_before_semicolon", "nspace_before_struct_op", "nspace_after_binary_op",
                "nspace_after_closing_brace", "nspace_after_comma", "nspace_after_keyword","nspace_after_opening_brace", "nspace_after_semicolon", "nno_space_after_struct_op",
                "nspace_before_binary_op", "nspace_before_closing_brace", "nspace_before_keyword","nspace_before_opening_brace", "nno_space_before_struct_op",
                "nno_space_after_opening_square_bracket", "nno_space_after_unary_op","nno_space_before_closing_bracket", "nno_space_before_closing_square_bracket",
                "nno_space_before_comma", "nno_space_before_opening_square_bracket","nno_space_before_semicolon"};
        String[] prestHeader={"Total_loc","blank_loc","comment_loc","code_and_comment_loc","executable_loc","unique_operands",
                "unique_operators","total_operands","total_operators","halstead_vocabulary","halstead_length","halstead_volume","halstead_level",
                "halstead_difficulty","halstead_effort","halstead_error","halstead_time","branch_count","decision_count","call_pairs","condition_count",
                "multiple_condition_count","cyclomatic_complexity","cyblomatic_density","decision_density","design_complexity","design_density",
                "norm_cyclomatic_complexity","formal_parameters"};
        List<String> list=new ArrayList<String>();
        list.addAll(java.util.Arrays.asList(cccHeader));

    }
}
