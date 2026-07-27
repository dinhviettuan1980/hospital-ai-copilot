"""
Seed the weekly-report Postgres (Neon) DB with data manually transcribed
from 3 real weekly briefing decks ("Báo cáo giao ban tuần") of Bệnh viện
Tim Hà Nội.

This is a stand-in for the future python-pptx ingestion pipeline: the 3
source files are treated as already ingested, so the AI Director layer can
be tested end-to-end before the real parser exists.

Requires DATABASE_URL in hospital-ai/.env (Neon Postgres connection string).

Run: python3 seed_data.py
"""
import os
from pathlib import Path

import psycopg
from dotenv import load_dotenv

load_dotenv(Path(__file__).parent.parent / ".env")
DATABASE_URL = os.environ["DATABASE_URL"]

SCHEMA = """
DROP TABLE IF EXISTS metric_value, incident, feedback, narrative_section,
    report_period, facility, metric CASCADE;

CREATE TABLE report_period (
    id SERIAL PRIMARY KEY,
    label TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    source_file TEXT NOT NULL
);

CREATE TABLE facility (
    id SERIAL PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL
);

CREATE TABLE metric (
    id SERIAL PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    category TEXT NOT NULL,
    name TEXT NOT NULL,
    unit TEXT
);

CREATE TABLE metric_value (
    id SERIAL PRIMARY KEY,
    report_period_id INTEGER NOT NULL REFERENCES report_period(id),
    facility_id INTEGER NOT NULL REFERENCES facility(id),
    metric_id INTEGER NOT NULL REFERENCES metric(id),
    value NUMERIC,
    note TEXT
);

CREATE TABLE incident (
    id SERIAL PRIMARY KEY,
    report_period_id INTEGER NOT NULL REFERENCES report_period(id),
    department TEXT,
    incident_date TEXT,
    description TEXT,
    cause TEXT,
    corrective_action TEXT,
    resolved BOOLEAN,
    severity TEXT
);

CREATE TABLE feedback (
    id SERIAL PRIMARY KEY,
    report_period_id INTEGER NOT NULL REFERENCES report_period(id),
    date TEXT,
    department TEXT,
    type TEXT,
    content TEXT,
    cause TEXT,
    resolution TEXT
);

CREATE TABLE narrative_section (
    id SERIAL PRIMARY KEY,
    report_period_id INTEGER NOT NULL REFERENCES report_period(id),
    section_name TEXT,
    content TEXT
);
"""

FACILITIES = [
    ("CS1", "Cơ sở 1"),
    ("CS2", "Cơ sở 2"),
    ("TOTAL", "Toàn viện (2 cơ sở)"),
]

METRIC_DEFS = [
    # code, category, name, unit
    ("tong_bn_kham", "kham_benh", "Tổng số BN khám bệnh", "lượt"),
    ("kham_cap_cuu", "kham_benh", "Khám cấp cứu", "lượt"),
    ("kham_tim_mach", "kham_benh", "Khám tim mạch", "lượt"),
    ("dieu_tri_ban_ngay", "kham_benh", "Điều trị ban ngày", "lượt"),
    ("bn_vao_vien", "noi_tru", "Tổng số BN vào viện", "BN"),
    ("bn_ra_vien", "noi_tru", "Tổng số BN ra viện", "BN"),
    ("bn_chuyen_vien", "noi_tru", "Tổng số BN chuyển viện", "BN"),
    ("bn_tuvong_xinve", "noi_tru", "Tổng số BN tử vong/xin về", "BN"),
    ("can_thiep_tim_mach", "can_thiep", "Tim mạch can thiệp", "ca"),
    ("can_thiep_bien_chung", "can_thiep", "Can thiệp có biến chứng", "ca"),
    ("can_thiep_mach_mau", "can_thiep", "Can thiệp mạch máu (tĩnh mạch/động mạch ngoại biên)", "ca"),
    ("so_stent", "can_thiep", "Số Stent đã đặt", "cái"),
    ("tong_phau_thuat", "phau_thuat", "Tổng số ca phẫu thuật", "ca"),
    ("pt_tim_ho", "phau_thuat", "Phẫu thuật tim hở", "ca"),
    ("pt_tim_kin", "phau_thuat", "Phẫu thuật tim kín", "ca"),
    ("pt_cap_cuu", "phau_thuat", "Phẫu thuật cấp cứu", "ca"),
    ("pt_van_tim", "phau_thuat", "Phẫu thuật van tim", "ca"),
    ("pt_cau_noi", "phau_thuat", "Phẫu thuật cầu nối", "ca"),
    ("pt_tim_bam_sinh", "phau_thuat", "Phẫu thuật tim bẩm sinh", "ca"),
    ("chup_msct", "cdha", "Chụp MSCT", "lượt"),
    ("chup_cong_huong_tu", "cdha", "Chụp cộng hưởng từ (MRI)", "lượt"),
    ("chup_xquang", "cdha", "Chụp X quang", "lượt"),
    ("sieu_am_tim", "cdha", "Siêu âm tim", "lượt"),
    ("sieu_am_mach", "cdha", "Siêu âm mạch", "lượt"),
    ("sieu_am_bung", "cdha", "Siêu âm bụng", "lượt"),
    ("dien_tim", "cdha", "Điện tim (ECG)", "lượt"),
    ("noi_soi_da_day", "cdha", "Nội soi dạ dày tá tràng", "ca"),
    ("xn_huyet_hoc", "xet_nghiem", "Xét nghiệm Huyết học", "xét nghiệm"),
    ("xn_hoa_sinh", "xet_nghiem", "Xét nghiệm Hóa sinh", "xét nghiệm"),
    ("xn_vi_sinh", "xet_nghiem", "Xét nghiệm Vi sinh", "xét nghiệm"),
    ("don_vi_mau", "xet_nghiem", "Số đơn vị máu đã phát", "đơn vị"),
    ("doanh_thu_tuan", "tai_chinh", "Doanh thu tuần (viện phí + dịch vụ)", "triệu đồng (đơn vị ghi trong bản gốc, có thể là tỷ - xem note)"),
]

# --- Report 1: 20/04/2022 - 26/04/2022 ---------------------------------
R1 = {
    "label": "Tuần 20/04 - 26/04/2022",
    "start_date": "2022-04-20",
    "end_date": "2022-04-26",
    "source_file": "15 - Bao cao giao ban tuan cs1 20-04-2022 đến 26-04-2022.ppt",
    "metrics": {
        "tong_bn_kham": {"CS1": 4378, "CS2": 4869, "TOTAL": 9247},
        "kham_cap_cuu": {"CS1": 227, "CS2": 77, "TOTAL": 304},
        "kham_tim_mach": {"CS1": 3886, "CS2": 3701, "TOTAL": 7587},
        "dieu_tri_ban_ngay": {"CS1": 265, "TOTAL": 442},
        "bn_vao_vien": {"CS1": 178, "CS2": 97, "TOTAL": 275},
        "bn_ra_vien": {"CS1": 195, "CS2": 93, "TOTAL": 288},
        "bn_chuyen_vien": {"CS1": 6, "CS2": 4, "TOTAL": 10},
        "bn_tuvong_xinve": {"CS1": 2, "TOTAL": 2},
        "can_thiep_tim_mach": {"CS1": 202, "CS2": 77, "TOTAL": 279},
        "can_thiep_bien_chung": {"TOTAL": 1},
        "can_thiep_mach_mau": {"CS1": 20, "CS2": 0, "TOTAL": 20},
        "so_stent": {"TOTAL": 139},
        "tong_phau_thuat": {"TOTAL": 49},
        "pt_tim_ho": {"TOTAL": 45},
        "pt_tim_kin": {"TOTAL": 4},
        "pt_cap_cuu": {"TOTAL": 6},
        "pt_van_tim": {"TOTAL": 21},
        "pt_cau_noi": {"TOTAL": 7},
        "pt_tim_bam_sinh": {"TOTAL": 17},
        "chup_msct": {"CS1": 0, "CS2": 319, "TOTAL": 319},
        "chup_cong_huong_tu": {"TOTAL": 99},
        "chup_xquang": {"CS1": 1853, "CS2": 911, "TOTAL": 2764},
        "sieu_am_tim": {"CS1": 2427, "CS2": 1534, "TOTAL": 3961},
        "sieu_am_mach": {"CS1": 1533, "CS2": 1483, "TOTAL": 3016},
        "sieu_am_bung": {"CS1": 1321, "TOTAL": 2159},
        "dien_tim": {"CS1": 4090, "CS2": 4066, "TOTAL": 8156},
        "noi_soi_da_day": {"TOTAL": 51},
        "xn_huyet_hoc": {"CS1": 7221, "CS2": 2762, "TOTAL": 9983},
        "xn_hoa_sinh": {"CS1": 37653, "CS2": 23245, "TOTAL": 60898},
        "xn_vi_sinh": {"CS1": 87, "CS2": 14, "TOTAL": 101},
        "don_vi_mau": {"CS1": 219, "TOTAL": 229},
        "doanh_thu_tuan": {"CS1": 24380, "CS2": 8575, "TOTAL": 32955,
                            "note": "Đơn vị ghi trong bảng gốc là 'Triệu' nhưng mục Dược cùng tuần lại ghi tổng doanh thu 7,829 tỷ đồng - đơn vị trong bản gốc không nhất quán, cần đối chiếu lại."},
    },
    "incidents": [
        {
            "department": "Khoa Hồi sức ngoại",
            "incident_date": "2022-04",
            "description": "Đợt kiểm tra nội bộ tại khoa Hồi sức ngoại phát hiện: bảng nhận diện rủi ro chưa đầy đủ (đặc biệt hoạt động của bác sỹ); không ghi nhận báo cáo sự cố y khoa trong năm 2021; phòng can thiệp số 2 nhiều ngày trong tháng 4 độ ẩm vượt tiêu chuẩn (đã xin bổ sung máy hút ẩm từ 08/2021 nhưng chưa được trang bị); tủ vật tư tiêu hao chưa có nhãn phân loại.",
            "cause": "Thiếu nhận diện rủi ro đầy đủ; chưa phổ biến quy trình quản lý sự cố y khoa; chậm trang bị máy hút ẩm.",
            "corrective_action": "Bổ sung nhận diện rủi ro hoạt động bác sỹ; phổ biến quy trình quản lý SCYK và xác định đúng người bệnh; đề nghị phòng HCQT sớm trang bị máy hút ẩm.",
            "resolved": 0,
            "severity": "medium",
        },
        {
            "department": "Toàn viện",
            "incident_date": "2022-04-27",
            "description": "Đoàn kiểm tra Sở Y tế đánh giá chất lượng bệnh viện: điểm đạt CS1 4.31, CS2 4.33 (thang 83 tiêu chí); đánh giá bệnh viện an toàn phòng chống Covid 93.9%. Một số tồn tại: tủ đồ dùng người bệnh lộn xộn; giấy hướng dẫn dán tường cần thay; tranh ảnh vật dụng để sai quy định; hộp đựng chỉ định/kết quả xét nghiệm bằng giấy cactong cần thay hộp nhựa/mika; dây điện các khoa phòng cần sắp xếp gọn gàng.",
            "cause": "Chưa hoàn thiện đầy đủ theo tiêu chí 5S/an toàn người bệnh tại một số khu vực.",
            "corrective_action": "Khắc phục theo góp ý đoàn kiểm tra tại từng khoa/phòng liên quan.",
            "resolved": 0,
            "severity": "low",
        },
    ],
    "feedback": [
        {
            "date": "2022-04",
            "department": "Toàn viện",
            "type": "praise",
            "content": "Hòm thư góp ý: 4 thư khen tập thể (CS1: 4, CS2: 1 - theo bảng gốc), 0 thư khen cá nhân, 0 ý kiến góp ý.",
            "cause": None,
            "resolution": None,
        },
    ],
    "narrative": [
        ("Điều dưỡng", "Tổ chức đào tạo 'Chăm sóc bệnh nhân thở máy' cho Điều dưỡng khối Hồi sức - Cấp cứu - Các bệnh mạch máu. Sắp xếp nhân lực Điều dưỡng tham gia đội cấp cứu tiêm chủng. Kiểm tra công tác Điều dưỡng tại khoa Tim mạch can thiệp theo đoàn kiểm tra bệnh viện."),
        ("Dược", "Đảm bảo cung ứng thuốc. Đơn BHYT TB 1.463 đơn/ngày (CS1: 552, CS2: 910). Tổng doanh thu tuần theo mục Dược: 7,829 tỷ đồng (đơn thuốc BHYT ~4,854 tỷ, nhà thuốc dịch vụ ~2,975 triệu)."),
        ("Vật tư", "Đảm bảo cấp phát hàng hóa cho khoa phòng (nhập 37,1 tỷ, cấp phát 36,1 tỷ). Sửa chữa máy cưa điện, máy truyền dịch, cáng đẩy, máy điện tim, máy trao đổi nhiệt. Tiếp nhận 02 máy thở từ nguồn phòng chống dịch Bộ Y tế. Phê duyệt kế hoạch gói thầu Hóa chất số 21."),
        ("Đào tạo & NCKH", "Đào tạo liên tục chủ đề Nội khoa: Phì đại tiền liệt tuyến. Chuẩn bị tài liệu tiếp đoàn kiểm tra bệnh viện (tiêu chí 10)."),
        ("Đào tạo & Chỉ đạo tuyến", "Hỗ trợ khám chữa bệnh tại BV Ung bướu Hà Nội, CGKT can thiệp tim mạch tại BV C Thái Nguyên. Số liệu chuyển tuyến đến tuần (20-26/4/2022): 739 ca từ các đơn vị thuộc Hà Nội (nhiều nhất: BVĐK huyện Chương Mỹ 49 ca), 156 ca từ các đơn vị ngoại tỉnh (nhiều nhất: BVĐK tỉnh Cao Bằng 19 ca)."),
        ("Công tác xã hội", "Khám sàng lọc tim bẩm sinh: Hải Phòng khám 114 trẻ (19 trẻ cần can thiệp), Vĩnh Phúc khám 13.151 trẻ (5 trẻ cần can thiệp). Hỗ trợ kinh phí 03 ca can thiệp/phẫu thuật ~90 triệu đồng. Hỗ trợ 46 bệnh nhân ra viện."),
        ("Dinh dưỡng", "Tư vấn dinh dưỡng cho 94 BN. Lập kế hoạch can thiệp dinh dưỡng cho 16 BN suy dinh dưỡng. Cung cấp 1.310 suất ăn bệnh lý cho khu Covid (TB 187 suất/ngày)."),
    ],
}

# --- Report 2: 09/07/2025 - 15/07/2025 ----------------------------------
R2 = {
    "label": "Tuần 09/07 - 15/07/2025",
    "start_date": "2025-07-09",
    "end_date": "2025-07-15",
    "source_file": "26 - Bao cao giao ban tuan cs1 09-07-2025 đến 15-07-2025 FINAL.ppt",
    "metrics": {
        "tong_bn_kham": {"CS1": 6962, "CS2": 6687, "TOTAL": 13649},
        "kham_cap_cuu": {"CS1": 267, "CS2": 151, "TOTAL": 418},
        "kham_tim_mach": {"CS1": 6161, "CS2": 5080, "TOTAL": 11241},
        "dieu_tri_ban_ngay": {"CS1": 534, "TOTAL": 783},
        "bn_vao_vien": {"CS1": 218, "CS2": 115, "TOTAL": 333},
        "bn_ra_vien": {"CS1": 230, "CS2": 90, "TOTAL": 320},
        "bn_chuyen_vien": {"CS1": 9, "CS2": 10, "TOTAL": 19},
        "bn_tuvong_xinve": {"CS1": 2, "CS2": 0, "TOTAL": 2},
        "can_thiep_tim_mach": {"CS1": 233, "CS2": 92, "TOTAL": 325},
        "can_thiep_bien_chung": {"TOTAL": 0},
        "can_thiep_mach_mau": {"TOTAL": 38, "note": "Tĩnh mạch 29, động mạch ngoại biên 9"},
        "tong_phau_thuat": {"TOTAL": 48},
        "pt_tim_ho": {"TOTAL": 44},
        "pt_tim_kin": {"TOTAL": 4},
        "pt_cap_cuu": {"TOTAL": 8},
        "pt_van_tim": {"TOTAL": 24},
        "pt_cau_noi": {"TOTAL": 7},
        "chup_msct": {"CS1": 487, "TOTAL": 487},
        "chup_cong_huong_tu": {"TOTAL": 90},
        "chup_xquang": {"CS1": 1483, "CS2": 673, "TOTAL": 2156},
        "sieu_am_tim": {"CS1": 2803, "CS2": 1358, "TOTAL": 4161},
        "sieu_am_mach": {"CS1": 1891, "CS2": 990, "TOTAL": 2881},
        "sieu_am_bung": {"CS1": 1288, "TOTAL": 2214},
        "dien_tim": {"CS1": 6873, "CS2": 5603, "TOTAL": 12476},
        "noi_soi_da_day": {"TOTAL": 50},
        "xn_huyet_hoc": {"CS1": 5934, "CS2": 2815, "TOTAL": 8749},
        "xn_hoa_sinh": {"CS1": 48343, "CS2": 26406, "TOTAL": 74749},
        "xn_vi_sinh": {"CS1": 95, "CS2": 18, "TOTAL": 113},
        "don_vi_mau": {"TOTAL": 293},
        "doanh_thu_tuan": {"CS1": 38692, "CS2": 14512, "TOTAL": 53204,
                            "note": "Đơn vị ghi trong bảng gốc là 'Triệu' nhưng mục Dược cùng tuần lại ghi tổng doanh thu 15,878 tỷ đồng - đơn vị trong bản gốc không nhất quán, cần đối chiếu lại."},
    },
    "incidents": [
        {
            "department": "Khoa Khám bệnh",
            "incident_date": "2025-07-10",
            "description": "Bác sĩ kê đơn thuốc không có trong tồn kho dược nên phải hủy đơn và kê lại đơn thuốc cho người bệnh.",
            "cause": "Lỗi phần mềm không hiển thị đúng số lượng thuốc tồn trong kho thuốc BHYT.",
            "corrective_action": "Báo phòng CNTT kiểm tra khắc phục; phối hợp đơn vị cung cấp phần mềm phòng ngừa sự cố lặp lại.",
            "resolved": 1,
            "severity": "low",
        },
        {
            "department": "Khoa Nội Nhi",
            "incident_date": "2025-07-15",
            "description": "Hỏng điều hòa phòng bệnh số 02, ảnh hưởng tới sự hài lòng của người bệnh.",
            "cause": "Lỗi thiết bị.",
            "corrective_action": "Giải thích với người bệnh; báo HCQT kiểm tra khắc phục; bảo dưỡng định kỳ theo kế hoạch.",
            "resolved": 1,
            "severity": "low",
        },
    ],
    "feedback": [
        {
            "date": "2025-07-11",
            "department": "Khám bệnh tự nguyện 1 (KBTN1)",
            "type": "complaint",
            "content": "Người bệnh tiểu đường, đau tức ngực (hẹp mạch vành 60%) phản ánh bị nhân viên điều dưỡng quầy số 1 từ chối tiếp nhận đăng ký khám dù đến xếp hàng từ sáng, cảm thấy bị đối xử tắc trách.",
            "cause": "Ngày 10/7 lượng người bệnh đến khám rất đông (1300 BN, trong đó Tự nguyện 1 là 580 BN); nhân viên chưa giải thích rõ tình hình cho người bệnh.",
            "resolution": "Nhắc nhở nhân viên thực hiện nghiêm quy tắc giao tiếp ứng xử, đặc biệt tại vị trí tư vấn TN1; hướng dẫn người bệnh khám vào ngày hôm sau.",
        },
        {
            "date": "2025-07-14",
            "department": "KBTN1",
            "type": "complaint",
            "content": "Người bệnh góp ý 4 nội dung: tăng cường CNTT trong đặt lịch/thanh toán; phân luồng ưu tiên hợp lý hơn; bổ sung nhân sự giờ cao điểm để giảm thời gian chờ; tăng cường biển chỉ dẫn.",
            "cause": "Lượng bệnh nhân đông, đối tượng ưu tiên chiếm 30-40%, cơ sở hạ tầng chật hẹp.",
            "resolution": "Bệnh viện đã triển khai đặt lịch/thanh toán QR online; đang tăng cường điều chỉnh hẹn khám theo khung giờ; đang hoàn thiện bệnh án điện tử.",
        },
        {
            "date": "2025-07-tuần",
            "department": "Toàn viện",
            "type": "praise",
            "content": "Hòm thư góp ý: 01 thư khen tập thể được ghi nhận (CS2).",
            "cause": None,
            "resolution": None,
        },
    ],
    "narrative": [
        ("Điều dưỡng", "Tổ chức báo cáo ca bệnh - bình kế hoạch chăm sóc cho Điều dưỡng khối Hồi sức - Cấp cứu. Tư vấn giáo dục sức khỏe cho bệnh nhân trước phẫu thuật. Đào tạo điều dưỡng học việc về thuốc tim mạch thường gặp."),
        ("Dược", "Thẩm định KQLCNT gói thầu bổ sung thuốc Biệt dược gốc. Đơn BHYT TB 2.175 đơn/ngày (CS1: 946, CS2: 1.228), tổng doanh thu tuần 11,078 tỷ đồng. Nhà thuốc: TB 1.014 đơn/ngày, doanh thu 4,800 tỷ đồng. Tổng doanh thu Dược: 15,878 tỷ đồng."),
        ("Quản lý chất lượng", "Tham gia đoàn kiểm tra bệnh viện tại Phòng khám đa khoa. Giám sát nhân viên xác định đúng người bệnh tại 2 cơ sở. Xây dựng kế hoạch tiếp đoàn đánh giá chứng nhận ISO 9001:2015. Có 2 sự cố y khoa và 2 khiếu nại được ghi nhận trong tuần (xem bảng incident/feedback)."),
        ("Đào tạo & Chỉ đạo tuyến", "Telehealth buổi 115 (16/7/2025): hội chẩn 5 đơn vị (BVĐK Cao Bằng, BV Bãi Cháy, BVĐK KV miền núi phía Bắc Quảng Nam, TTYT Thanh Ba, BVĐK Sóc Sơn); 29 đơn vị dự thính/tập huấn; 7.100 lượt xem livestream. Hỗ trợ chuyên môn tại BVĐK huyện Mê Linh, BV Hữu Nghị Việt Nam Cu Ba, BVĐK Ngã Tư Hồ, BV Ung bướu HN, BV Mặt Trời."),
        ("Công tác xã hội", "Khám tặng quà đối tượng chính sách phường Hoàn Kiếm. Hỗ trợ kinh phí phẫu thuật/can thiệp cho 03 bệnh nhân khó khăn (~120 triệu đồng). Hỗ trợ bệnh nhân ra viện: 73 ca. Bảo lãnh viện phí cho khách hàng bảo hiểm (Bảo Việt, PVI, Vietin, Insmart, Daiichi, Pacific Cross, Bảo Minh, FPT IS, AIA)."),
        ("Dinh dưỡng", "Tư vấn dinh dưỡng cho 133 BN. Can thiệp dinh dưỡng cho 11 BN suy dinh dưỡng. Cung cấp 198 suất ăn bệnh lý."),
    ],
}

# --- Report 3: 03/06/2026 - 09/06/2026 ----------------------------------
R3 = {
    "label": "Tuần 03/06 - 09/06/2026",
    "start_date": "2026-06-03",
    "end_date": "2026-06-09",
    "source_file": "T23. Báo cáo giao ban tuần 3.6- 9.6.26.Final.pptx",
    "metrics": {
        "tong_bn_kham": {"CS1": 5854, "CS2": 5569, "TOTAL": 11423},
        "kham_cap_cuu": {"CS1": 209, "CS2": 200, "TOTAL": 409},
        "kham_tim_mach": {"CS1": 5217, "CS2": 4304, "TOTAL": 9521},
        "dieu_tri_ban_ngay": {"CS1": 428, "CS2": 0, "TOTAL": 428},
        "bn_vao_vien": {"CS1": 263, "CS2": 104, "TOTAL": 367},
        "bn_ra_vien": {"CS1": 248, "CS2": 71, "TOTAL": 319},
        "bn_chuyen_vien": {"CS1": 11, "CS2": 20, "TOTAL": 31},
        "bn_tuvong_xinve": {"CS1": 1, "CS2": 0, "TOTAL": 1},
        "can_thiep_tim_mach": {"CS1": 227, "CS2": 68, "TOTAL": 295},
        "can_thiep_bien_chung": {"TOTAL": 0},
        "can_thiep_mach_mau": {"TOTAL": 31, "note": "Tĩnh mạch 22, động mạch ngoại biên 9"},
        "tong_phau_thuat": {"TOTAL": 57},
        "pt_tim_ho": {"TOTAL": 48},
        "pt_tim_kin": {"TOTAL": 9},
        "pt_cap_cuu": {"TOTAL": 11},
        "pt_van_tim": {"TOTAL": 25},
        "pt_cau_noi": {"TOTAL": 13},
        "pt_tim_bam_sinh": {"TOTAL": 11},
        "chup_msct": {"TOTAL": 465, "note": "512 lát cắt 306, 128 lát cắt 159"},
        "chup_cong_huong_tu": {"TOTAL": 82, "note": "Máy CHT dừng chụp từ 4/6 đến 6/6"},
        "chup_xquang": {"CS1": 1405, "CS2": 785, "TOTAL": 2190},
        "sieu_am_tim": {"CS1": 2466, "CS2": 1300, "TOTAL": 3766},
        "sieu_am_mach": {"CS1": 1698, "CS2": 1231, "TOTAL": 2929},
        "sieu_am_bung": {"CS1": 1191, "CS2": 965, "TOTAL": 2156},
        "dien_tim": {"CS1": 5561, "CS2": 4553, "TOTAL": 10114},
        "noi_soi_da_day": {"TOTAL": 40},
        "xn_huyet_hoc": {"CS1": 5843, "CS2": 2520, "TOTAL": 8003},
        "xn_hoa_sinh": {"CS1": 44517, "CS2": 26459, "TOTAL": 70976},
        "xn_vi_sinh": {"CS1": 56, "CS2": 17, "TOTAL": 73},
        "don_vi_mau": {"CS1": 296, "TOTAL": 313},
        "doanh_thu_tuan": {"TOTAL": None,
                            "note": "Báo cáo tuần này không có bảng doanh thu tuần dạng như 2 báo cáo kia; chỉ có bảng đơn thuốc: BHYT CS1+CS2 8.241 đơn / 9.567.724.000đ, Nhà thuốc 4.681 đơn / 5.211.605.000đ."},
    },
    "incidents": [
        {
            "department": "Khoa Tim mạch can thiệp cơ sở 2",
            "incident_date": "2026-06-04",
            "description": "Màn hình trong hệ thống can thiệp mạch bị hỏng, không lên hình khi chiếu chụp -> không thực hiện can thiệp được cho người bệnh.",
            "cause": "Lỗi thiết bị y tế.",
            "corrective_action": "Báo cáo lãnh đạo, phòng VT-TBYT và kỹ sư hãng kiểm tra khắc phục kịp thời; kiểm tra bảo trì bảo dưỡng định kỳ.",
            "resolved": 1,
            "severity": "high",
        },
        {
            "department": "Khoa Khám bệnh",
            "incident_date": "2026-06-04",
            "description": "Từ 9h50-10h10 lỗi không đọc được kết quả điện tim trên phần mềm QLBV, ảnh hưởng thời gian khám và sự hài lòng người bệnh.",
            "cause": "Lỗi phần mềm.",
            "corrective_action": "Báo cáo lãnh đạo và phòng CNTT kiểm tra khắc phục; phối hợp đơn vị cung cấp phần mềm phòng ngừa sự cố lặp lại.",
            "resolved": 1,
            "severity": "low",
        },
        {
            "department": "Khoa Phẫu thuật tim trẻ em",
            "incident_date": "2026-06-07",
            "description": "Kim lấy máu đã sử dụng cho người bệnh đựng trong hộp kháng thủng (hộp đựng vật sắc nhọn) đâm lộ ra ngoài ở góc dưới đáy hộp, nhân viên y tế không để ý bị kim xượt qua tay gây trầy xước và chảy máu -> nguy cơ phơi nhiễm nghề nghiệp.",
            "cause": "Nhân viên không phát hiện kim tiêm lộ ra ngoài hộp kháng thủng; chưa thực hiện đúng quy trình xử lý vật sắc nhọn.",
            "corrective_action": "Xử trí vết thương đúng quy trình; xét nghiệm xác định nguy cơ phơi nhiễm cho nhân viên; báo KSNK theo dõi xử trí sau phơi nhiễm; nhắc nhở quy trình xử lý vật sắc nhọn; đề xuất thay mới hộp kháng thủng định kỳ.",
            "resolved": 1,
            "severity": "high",
        },
    ],
    "feedback": [
        {
            "date": "2026-06-05",
            "department": "Khoa Khám bệnh (phòng khám 312)",
            "type": "complaint",
            "content": "Người nhà phản ánh bác sĩ tại phòng khám 312 có thái độ chưa đúng mực khi bệnh nhân Nguyễn Thị Se (71 tuổi, suy tim, nhà xa) xin đổi lịch hẹn tái khám từ khung giờ 7h-8h sang 6h-7h để kịp xe ghép.",
            "cause": "Khung giờ 6h-7h đã kín lịch hẹn, áp lực công việc khiến bác sĩ giao tiếp chưa khéo léo.",
            "resolution": "Khoa đã liên hệ gia đình giải thích quy định quản lý lịch hẹn và xin lỗi về sự cố giao tiếp, gia đình đã thông cảm; nhắc nhở bác sĩ và nhân viên rút kinh nghiệm về giao tiếp ứng xử.",
        },
        {
            "date": "2026-06-08",
            "department": "Phòng Tài chính Kế toán",
            "type": "complaint",
            "content": "Người nhà bệnh nhân ở Cao Bằng (300km) phản ánh thủ tục ra viện: không có phiếu thu tiền gốc (đã mang về Cao Bằng), chỉ có ảnh chụp/bản photo, bệnh viện không chấp nhận giải quyết dù có cam kết.",
            "cause": "Người nhà không giữ phiếu thu gốc đúng hướng dẫn; quy định tài chính yêu cầu chứng từ gốc khi hoàn ứng viện phí.",
            "resolution": "Tiếp tục thực hiện nghiêm quy định kiểm tra chứng từ gốc; bổ sung lưu ý tại quầy thu ngân; báo cáo Ban Giám đốc xem xét giải pháp hỗ trợ phù hợp cho người bệnh ở xa.",
        },
        {
            "date": "2026-06-10",
            "department": "Khoa Nội 2",
            "type": "complaint",
            "content": "Người bệnh góp ý điều dưỡng khoa Nội 2 làm việc chưa đúng quy trình.",
            "cause": "Sau xác minh, người bệnh (Nguyễn Văn Quý) hoàn toàn hài lòng với đội ngũ NVYT khoa, có thể người nhà chưa hiểu rõ quy trình chăm sóc.",
            "resolution": "Bác sĩ và Điều dưỡng trưởng giải thích lại quy trình, gia đình hài lòng không còn thắc mắc; nhắc nhở NVYT thực hiện nghiêm quy trình chuyên môn.",
        },
    ],
    "narrative": [
        ("Điều dưỡng", "Đào tạo điều dưỡng học việc: Chăm sóc người bệnh Suy tim; Điện tim cơ bản và nhận biết rối loạn nhịp thường gặp. Tư vấn giáo dục sức khỏe trước phẫu thuật. Kiểm tra công tác Điều dưỡng Phòng khám đa khoa theo đoàn kiểm tra bệnh viện."),
        ("Vật tư", "Cấp phát hàng hóa cho khoa phòng (nhập hơn 25,08 tỷ, cấp phát hơn 24,43 tỷ). Sửa chữa máy thở, cưa điện, máy theo dõi bệnh nhân, máy hút trung tâm, máy hút áp lực thấp, máy giặt, máy chụp mạch CS2. Xây dựng giá kế hoạch gói thầu vật tư y tế 2026."),
        ("Dược", "Đánh giá hồ sơ mời thầu thuốc bổ sung lần 2 năm 2026 và thuốc điều chỉnh bổ sung của Nhà thuốc Bệnh viện. Hoàn thiện tính năng cảnh báo kê đơn trên phần mềm. Số đơn thuốc BHYT CS1+CS2: 8.241 đơn (9,57 tỷ đồng); Nhà thuốc: 4.681 đơn (5,21 tỷ đồng)."),
        ("Quản lý chất lượng", "Tham gia đoàn kiểm tra bệnh viện tại Phòng khám đa khoa. Giám sát xác định đúng người bệnh trực tiếp tại các khoa ngoại trú. Nhập phiếu khảo sát hài lòng nhân viên y tế tháng 5/2026 và hài lòng người bệnh quý II/2026. Hòm thư góp ý: 0 thư khen, 3 ý kiến góp ý qua hòm thư online (xem bảng feedback). Hệ thống báo cáo sự cố y khoa: 03 sự cố (xem bảng incident) - trong đó có 1 sự cố phơi nhiễm nghề nghiệp (kim tiêm) tại khoa Phẫu thuật tim trẻ em, mức độ đáng lưu ý nhất trong tuần."),
        ("Đào tạo & NCKH", "Tiếp nhận 21 học viên CKI Gây mê Hồi sức ĐH Y Hà Nội thực hành tại bệnh viện. Tổ chức thi thực hành lớp Tim mạch can thiệp cơ bản và Siêu âm Doppler tim cơ bản. Phối hợp Roche và Boston Scientific tổ chức hội thảo khoa học về rối loạn tuyến giáp/nguy cơ tim mạch và chiến lược điều trị tổn thương động mạch vành."),
        ("Đào tạo & Chỉ đạo tuyến", "Báo cáo công việc tuần 23: hỗ trợ CGKT tại BVĐK Chương Mỹ, BV HN Việt Nam Cu Ba, PKĐK TT thuộc TTYT phường Việt Hưng, BV Phụ Sản Hà Nội, BV Ung bướu HN. Xây dựng chương trình Telehealth buổi 136 (17/6/2026), chủ trì PGS.TS Nguyễn Sinh Hiền - Giám đốc BV. CGKT can thiệp tim mạch tại BVĐK TP Vinh."),
        ("Công tác xã hội", "Tiếp nhận quà tiền mặt/hiện vật 13 triệu đồng. Hỗ trợ kinh phí phẫu thuật/can thiệp cho 04 bệnh nhân khó khăn. Hỗ trợ 05 chuyến xe miễn phí. Kế hoạch khám sức khỏe, tặng quà 79 năm ngày Thương binh Liệt sĩ cho ~595 người tại xã Ô Diên và Liên Minh. Tổng đài 19001082: 972 cuộc gọi tư vấn/đặt lịch (805 trong giờ hành chính, 167 ngoài giờ). Bảo lãnh viện phí bảo hiểm: 11 ca, tổng 17.343.570đ."),
        ("Dinh dưỡng", "Tư vấn dinh dưỡng cho 155 BN. Can thiệp dinh dưỡng cho 32 BN suy dinh dưỡng. Cung cấp 232 suất ăn bệnh lý."),
    ],
}

REPORTS = [R1, R2, R3]


def main():
    conn = psycopg.connect(DATABASE_URL)
    cur = conn.cursor()
    cur.execute(SCHEMA)

    facility_ids = {}
    for code, name in FACILITIES:
        cur.execute("INSERT INTO facility (code, name) VALUES (%s, %s) RETURNING id", (code, name))
        facility_ids[code] = cur.fetchone()[0]

    metric_ids = {}
    for code, category, name, unit in METRIC_DEFS:
        cur.execute(
            "INSERT INTO metric (code, category, name, unit) VALUES (%s, %s, %s, %s) RETURNING id",
            (code, category, name, unit),
        )
        metric_ids[code] = cur.fetchone()[0]

    for report in REPORTS:
        cur.execute(
            "INSERT INTO report_period (label, start_date, end_date, source_file) "
            "VALUES (%s, %s, %s, %s) RETURNING id",
            (report["label"], report["start_date"], report["end_date"], report["source_file"]),
        )
        period_id = cur.fetchone()[0]

        for metric_code, facility_values in report["metrics"].items():
            note = facility_values.get("note")
            for fac_code in ("CS1", "CS2", "TOTAL"):
                if fac_code not in facility_values:
                    continue
                value = facility_values[fac_code]
                cur.execute(
                    "INSERT INTO metric_value (report_period_id, facility_id, metric_id, value, note) "
                    "VALUES (%s, %s, %s, %s, %s)",
                    (period_id, facility_ids[fac_code], metric_ids[metric_code], value, note),
                )

        for inc in report["incidents"]:
            cur.execute(
                "INSERT INTO incident (report_period_id, department, incident_date, description, "
                "cause, corrective_action, resolved, severity) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                (
                    period_id, inc["department"], inc["incident_date"], inc["description"],
                    inc["cause"], inc["corrective_action"], bool(inc["resolved"]), inc["severity"],
                ),
            )

        for fb in report["feedback"]:
            cur.execute(
                "INSERT INTO feedback (report_period_id, date, department, type, content, cause, resolution) "
                "VALUES (%s, %s, %s, %s, %s, %s, %s)",
                (period_id, fb["date"], fb["department"], fb["type"], fb["content"], fb["cause"], fb["resolution"]),
            )

        for section_name, content in report["narrative"]:
            cur.execute(
                "INSERT INTO narrative_section (report_period_id, section_name, content) VALUES (%s, %s, %s)",
                (period_id, section_name, content),
            )

    conn.commit()

    counts = {}
    for table in ("report_period", "facility", "metric", "metric_value", "incident", "feedback", "narrative_section"):
        cur.execute(f"SELECT COUNT(*) FROM {table}")
        counts[table] = cur.fetchone()[0]
    cur.close()
    conn.close()

    print("Seeded Neon Postgres DB")
    for table, count in counts.items():
        print(f"  {table}: {count} rows")


if __name__ == "__main__":
    main()
