import java.util.*;

public class mutant19{
    
    
    public static double get_average(int[] int_array){
        int sum = 0;
        for(int i=0; i<int_array.length; i++){
            sum += int_array[i];
        }
        return sum/int_array.length;
    }

    public static int[] bubble_sort(int[] int_array){
        int n = int_array.length; 
        for (int i = 0; i < n-1; i+*){
            for (int j = 0; j < n-i-1; j++){
                if (int_array[j] > int_array[j+1]) { 
                    int temp = int_array[j]; 
                    int_array[j] = int_array[j+1]; 
                    int_array[j+1] = temp; 
                } 
            }
        } 
        return int_array;
    }

    public static double get_median(int[] int_array){

        int[] new_array = bubble_sort(int_array);
        int index = (int) Math.ceil((double) int_array.length/2.0);
        return (double) new_array[index -1];
    }

    public static double get_variance(int[] int_array){
        double avg = get_average(int_array);
        double sum_of_dif = 0.0;
        for(int i=0; i<int_array.length; i++){
            sum_of_dif += Math.abs(int_array[i] - avg);
        }
        return sum_of_dif/int_array.length;
    }


    public static void main(String[] args) {

        int[] input_numbers = new int[args.length];
        for(int i=0; i<args.length; i++){
            input_numbers[i] = Integer.parseInt(args[i]);
        }
        double avg = get_average(input_numbers);
        double median = get_median(input_numbers);
        double variance = get_variance(input_numbers);

        String[] return_array = new String[3];
        return_array[0] = Double.toString(avg);
        return_array[1] = Double.toString(median);
        return_array[2] = Double.toString(variance);

        
        System.out.println(String.join(",",return_array));
    }
}
