import java.util.*;

public class Solution {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> ret = new ArrayList<>();
        if (candidates.length == 0)
            return ret;
        int low = 0, high = candidates.length - 1;
        while (high >= low) {
            List<Integer> cur = new ArrayList<>();
            int floatHigh = high;
            int index, floatTarget = target;
            while((index = binarySearch(candidates, floatTarget, low, floatHigh)) != -1) {
                floatTarget -= candidates[index];
                cur.add(candidates[index]);
                floatHigh = index;
            }
            if (floatTarget == 0) {
                ret.add(cur);
            }
            high--;
        }
        return ret;
    }

    public int binarySearch(int[] candidates, int x, int low, int high) {
        if (low > high)
            return -1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (candidates[mid] == x)
                return mid;
            else if (candidates[mid] < x)
                low = mid+1;
            else
                high = mid-1;
        }
        return low - 1;
    }

    public static void main(String[] args) {
        int[] arr = {2, 3, 6, 7};
        Solution s = new Solution();
        System.out.println(s.combinationSum(arr, 7));
    }
}