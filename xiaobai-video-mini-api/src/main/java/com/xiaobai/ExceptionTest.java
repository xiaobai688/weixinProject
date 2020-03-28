package com.xiaobai;


public class ExceptionTest {

    public static void main(String[] args) {
        try {
            test(new int[]{0,1,2,3,4,5});
        }catch (Exception e){
            System.out.println("E");
        }
    }

    private static void test(int[] arr){
        for(int i=0;i<arr.length;i++){
            try{
                if(arr[i]%2!=0){
                    throw new NullPointerException();
                }else{
                    System.out.println(i);
                }
            }finally {
                System.out.println("e");
            }
        }
    }
}
