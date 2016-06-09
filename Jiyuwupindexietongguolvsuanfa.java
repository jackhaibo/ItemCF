package xietong;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Jiyuwupindexietongguolvsuanfa {
	HashMap<Integer,Set<Integer>> trainset=new HashMap<Integer,Set<Integer>>();
	HashMap<Integer,Set<Integer>> testset=new HashMap<Integer,Set<Integer>>();
	HashMap<Integer,Integer> movie_popular=new HashMap<Integer,Integer>();
	int i=0;
	int trainset_length;
	int testset_length;
	int item_sim_mat[][];
	double  item_simlarity[][];
	int movie_count=0;
	List<Rank> recommendedMoviesList=null;
	List<Rank> ralatedMovieList=null;
    int k=0;
    int temp_k=0;
	int n=10;
	int temp_n=10;
	Random random=new Random(0);

	public void generate_dataset(int pivot) throws IOException{
		
		File file=new File("E:\\workspace\\ml-1m\\ratings.dat");
		
		if(!file.exists()||file.isDirectory())
			throw new FileNotFoundException();
		
		BufferedReader br=new BufferedReader(new FileReader(file));
		String temp=null;
		
		while ((temp=br.readLine())!=null) {
			
			String[] content=temp.replaceAll("\n\t", "").split("::");
			if(random.nextInt(8)==pivot){
				if(testset.containsKey(Integer.parseInt(content[0]))){
					HashSet<Integer> set =(HashSet<Integer>) testset.get(Integer.parseInt(content[0]));
					set.add(Integer.parseInt(content[1]));
					testset.put(Integer.parseInt(content[0]),set);
				}else{
					Set<Integer> set=new HashSet<Integer>();
					set.add(Integer.parseInt(content[1]));
					testset.put(Integer.parseInt(content[0]),set);
				}
				testset_length++;
				
			}else{
				if(trainset.containsKey(Integer.parseInt(content[0]))){
					HashSet<Integer> set =(HashSet<Integer>) trainset.get(Integer.parseInt(content[0]));
					set.add(Integer.parseInt(content[1]));
					trainset.put(Integer.parseInt(content[0]),set);
					
				}else{
					Set<Integer> set=new HashSet<Integer>();
					set.add(Integer.parseInt(content[1]));
					trainset.put(Integer.parseInt(content[0]),set);
				}
				
				trainset_length++;
				
			}
			i++;
			if (i%100000 == 0)
                System.out.println("已装载"+i+"文件");
	   }
		System.out.println("测试集和训练集分割完成，测试集长度："+testset_length+",训练集长度："+trainset_length);
		
	}
	
	// build inverse table for item-users
    // key=movieID, value=list of userIDs who have seen this movie
	public void calc_user_sim(){
		
		for(int obj : trainset.keySet()){ 
			
			Set<Integer> value = trainset.get(obj );
			Iterator<Integer> it=value.iterator();
			
		       while(it.hasNext())
		       {
		           int o=it.next();
		           //  count item popularity at the same time
		           if(!movie_popular.containsKey(o)){
		        	   movie_popular.put(o,1);
		           }else {
		        	   movie_popular.put(o,movie_popular.get(o)+1);
				   }
		          
		       }
			
			
			
			}
		
		//建立反转表的目的是方便建立co-rated movies 矩阵
		movie_count=movie_popular.size();
		System.out.println("movie number is"+movie_count);		
		System.out.println("building user co-rated movies matrix...");
		//有3953部电影
		item_sim_mat=new int[3953][3953];	
		
		for(int user : trainset.keySet()){
			
			Set<Integer> movies=trainset.get(user);
			Iterator<Integer> u=movies.iterator();
			
			while(u.hasNext()){
				int i=u.next();
				Iterator<Integer> v=movies.iterator();
				while(v.hasNext()){
					int j=v.next();
					if(i==j){
						continue;
					}else {
						item_sim_mat[i][j]+=1;
					}
						
					
				}
			}
		}
		
		System.out.println("co-rated movies矩阵创建成功");
		System.out.println("calculating item similarity matrix...");
		

		
		item_simlarity=new double[3953][3953];
		for(int i=0;i<3953;i++)
			for(int j=0;j<3953;j++){
				if(item_sim_mat[i][j]==0)
					continue;
				item_simlarity[i][j]=item_sim_mat[i][j]/Math.sqrt(movie_popular.get(i)*movie_popular.get(j)*1.0);
				
			}
		
		System.out.println("item_simlarity矩阵创建成功");	 
	}
	//对用户每部看过的电影进行相似推荐，取前k=20部，最后综合全部推荐结果，再取前n=10部
	public void recommend(int user){	
		recommendedMoviesList=new ArrayList<Rank>();
		Set<Integer> watched_movies=trainset.get(user);
		Iterator<Integer> it=watched_movies.iterator();
		while(it.hasNext()){
			int movie=it.next();
			ralatedMovieList=new ArrayList<Rank>();
			//把观看过的电影的关联电影加入list中
			for(int i=0;i<3953;i++){
				if(item_simlarity[movie][i]!=0.0){
					Rank rank=new Rank();
					rank.setMovie(i);
					rank.setSum_simlatrity(item_simlarity[movie][i]);
					ralatedMovieList.add(rank);
				}	
			}
			//堆排序，取前20部
			if(ralatedMovieList.size()>k){
				Heapsort ss=new Heapsort();
				ss.sort(ralatedMovieList, k);
			}
			
			if(ralatedMovieList.size()>0){
				if(ralatedMovieList.size()<=k) 
					k=ralatedMovieList.size();
			for(int j=0;j<k;j++){
				if(watched_movies.contains(ralatedMovieList.get(j).getMovie())){
					continue;
				}
				int index=recommendedMoviesList.indexOf(ralatedMovieList.get(j));
				if(index>-1){
					double simlarity=recommendedMoviesList.get(index).getSum_simlatrity()+ralatedMovieList.get(j).getSum_simlatrity();
					recommendedMoviesList.get(index).setSum_simlatrity(simlarity);
				}else{
					recommendedMoviesList.add(ralatedMovieList.get(j));
				}
				
			}

			
		}
			k=temp_k;
	}
		
		if(recommendedMoviesList.size()>n){
			Heapsort ss=new Heapsort();
			ss.sort(recommendedMoviesList, n);
		}
		
		
	}

	
	public void evaluate(){
		int count=0;
		int rec_count=0;
		int test_count=0;
		int hit=0;
		double popularSum=0;
		Set<Integer> all_rec_movies=new HashSet<Integer>();
		Iterator<Integer> it=trainset.keySet().iterator();
		while(it.hasNext()){
			int user=it.next();
			if(user%500==0)
				System.out.println("已经推荐了"+user+"个用户");
			
			Set<Integer> test_movies=testset.get(user);
			recommend(user);
			
			if(recommendedMoviesList!=null&&test_movies!=null){
				if(recommendedMoviesList.size()<n)
					n=recommendedMoviesList.size();
			for(int i=0;i<n;i++){
				Rank rec_movie=recommendedMoviesList.get(i);
				if(test_movies.contains(rec_movie.getMovie())){
					hit++;
				}
				all_rec_movies.add(rec_movie.getMovie());
				popularSum+=Math.log(1+movie_popular.get(rec_movie.getMovie()));
				count+=1;
			}
			
			rec_count+=n;
			test_count+=test_movies.size();
			}
			n=temp_n;
		}
		
		double precision=hit/(1.0*rec_count);
		double recall=hit/(1.0*test_count);
		double coverage=all_rec_movies.size()/(1.0*movie_count);
		double popularity=popularSum/(1.0*rec_count);
		System.out.println("precision=%"+precision*100+"\trecall=%"+recall*100+"\tcoverage=%"+coverage*100+"\tpopularity="+popularity);
		
	}
	
	
	public static void main(String[] args) throws IOException {
		Jiyuwupindexietongguolvsuanfa ss=new Jiyuwupindexietongguolvsuanfa();
		ss.generate_dataset(3);
		ss.calc_user_sim();
		List<Integer> set=new ArrayList<Integer>();
		set.add(5);
		set.add(10);
		set.add(20);
		set.add(40);
		set.add(80);
		set.add(160);
		for(int i=0;i<set.size();i++){
			ss.k=set.get(i);
			ss.temp_k=set.get(i);
			ss.evaluate();
		}

	}


}
