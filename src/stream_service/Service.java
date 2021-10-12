package stream_service;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import homebook.dao.HomeBookDAO;
import homebook.dao.IDao;
import homebook.tools.ConnectionFactory;
import homebook.vo.HomeBook;

public class Service {
	private Connection conn;
	private IDao dao;

	public Service() {
		try {
			this.conn=ConnectionFactory.create();
			this.dao=new HomeBookDAO();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws SQLException {
		Service service = new Service();
		service.test(service.conn, service.dao);
	}
	
	// 범용서비스를 위한 메소드 만들어보기
	
	//allData얻는 메소드
	public List<HomeBook> getAllData(){
		List<HomeBook> data = null;
		try {
			data = dao.selectAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	//지정한 날짜의 자료를 출력해주는 서비스
	public List<HomeBook> getSomedayData(int year, int month, int day){
		Predicate<HomeBook> pre = h->{
			Timestamp ts = h.getDay();
			boolean a = ts.getYear()==year-1900; // 1900 빼기
			boolean b = ts.getMonth()==month-1; // 1 빼기
			boolean c = ts.getDate()==day;
			return a&&b&&c;
		};
		return getAllData().stream()
				.filter(pre).collect(Collectors.toList());

	}
	//지정 기간의 자료를 출력해주는 서비스
	public List<HomeBook> getTermData(Timestamp a, Timestamp b){
		Predicate<HomeBook> pre = h->{
			Timestamp ts = h.getDay();
			return (ts.after(a)&&ts.after(b)
					||ts.equals(a)
					||ts.equals(b));
		};
		return getAllData().stream().filter(pre).collect(Collectors.toList());
	}
	
	//지정한 금액 이상의 지출을 획득한 목록들
	public List<HomeBook> moreThanData(long amount){
		return getAllData().stream()
				.filter(h->h.getSection().equals("지출"))
				.filter(h->h.getExpense() >= amount)
				.collect(Collectors.toList());	
	}
	//지정한 계정과목 데이터 목록
	public List<HomeBook> certainAccountTitle(String title){
	
		return getAllData().stream()
				.filter(h->h.getAccounttitle().equals(title))
				.collect(Collectors.toList());	
	}
	//지정한 계정과목의 합계 얻기
	public long certainAccountTitleSum(String title) {
		
		return getAllData().stream()
				.filter(h->h.getAccounttitle().equals(title))
				.mapToLong(h->(h.getSection().equals("수입"))?h.getRevenue():h.getExpense())
				.sum();
	}
	//사용된 계정과목 추출하기
	public List<String> getUsedTitle(){
		return getAllData().stream()
				.map(h->h.getAccounttitle()).distinct()
				.collect(Collectors.toList());
		
	}
	
	//가계부 데이터를 계정과목별로 분류하여 집계출력하는 서비스
		public void sumPrintService() {
		
			List<String> usedT = getUsedTitle();
			for(String x :usedT) {
				System.out.println(x+":"+certainAccountTitleSum(x));
			}
		}

	public int test(Connection conn, IDao dao) {
		int res = 0;
		try {
			List<HomeBook> allData = dao.selectAll();
			allData.stream().forEach(x->System.out.println(x));
			//차변(수입)합계
			long 차변합계 = allData.stream().mapToLong(h->h.getRevenue()).sum();
			//대변(지출)합계
			long 대변합계 = allData.stream().mapToLong(h->h.getExpense()).sum();
			
			//사용된 지출과목 리스트 얻기
			System.out.println(">>>>사용된 지출과목 리스트 얻기<<<<");
			List<String> 지출계정목록 = 
				allData.stream()
				.filter(h->h.getSection().equals("지출")) 
				.map(h->h.getAccounttitle()).distinct()
				.collect(Collectors.toList());
			getUsedTitle().stream().forEach(System.out::println);
			
			//지정한 날짜의 가계부 목록들
			System.out.println(">>>>지정한 날짜의 가계부 목록들<<<<");
			getSomedayData(2021,6,24).stream()
			.forEach(d->System.out.println(d)); //System.out::println
			
			//지정한 기간의 가계부 목록들
//			getTermData(Timestamp a, Timestamp b)
//			Localdate a = new LocalDate("2021-6-1");
			Timestamp a = new Timestamp(121,5,1,0,0,0,0); // => 2021.06.01
			Timestamp b = new Timestamp(121,5,30,0,0,0,0);// => 2021.06.30
			getTermData(a,b).stream().forEach(System.out::println);

			//지출금액이 5000원 이상인 목록들
			System.out.println(">>>>지출금액이 5000원 이상인 목록들<<<<");
			List<HomeBook> 지정금액이상목록 = 
				allData.stream()
				.filter(h->h.getExpense()>=5000)
				.collect(Collectors.toList());
			지정금액이상목록.stream().forEach(System.out::println);
			
			//특정 계정 과목만 출력하기
			System.out.println(">>>>특정 계정 과목만 출력하기<<<<");
			allData.stream()
				.filter(h->h.getAccounttitle().equals("간식비"))
				.forEach(h->System.out.println(h));
			
			//주어진 지출 계정과목의 합계
			System.out.println(">>>>주어진 지출 계정과목의 합계<<<<");
			System.out.println("지출내역 합계 : "+certainAccountTitleSum("간식비"));
			
			//계정과목별 집계
			System.out.println(">>>>주어진 지출 계정과목의 합계<<<<");
			sumPrintService();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
}