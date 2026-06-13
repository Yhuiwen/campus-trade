package com.campus.trade.agent.service;

import com.campus.trade.agent.dto.AgentChatRequest;
import com.campus.trade.agent.dto.PriceAdviceRequest;
import com.campus.trade.agent.dto.PublishAdviceRequest;
import com.campus.trade.agent.vo.AgentChatVo;
import com.campus.trade.agent.vo.PriceAdviceVo;
import com.campus.trade.agent.vo.PublishAdviceVo;
import com.campus.trade.agent.vo.RecommendGoodsVo;
import com.campus.trade.agent.vo.RiskAnalyzeVo;
import com.campus.trade.common.BusinessException;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.Goods;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final PriceAdvisorService priceAdvisorService;
    private final RiskAnalyzeService riskAnalyzeService;
    private final RecommendationService recommendationService;
    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectProvider<AgentModelProvider> modelProviders;

    public PublishAdviceVo publishAdvice(PublishAdviceRequest request) {
        Category category = categoryMapper.selectById(request.categoryId());
        if (category == null) throw new BusinessException("商品分类不存在");
        String title = clean(request.title());
        String description = clean(request.description());
        PriceAdviceVo price = priceAdvisorService.advise(
                new PriceAdviceRequest(request.categoryId(), request.originalPrice(), description));
        List<String> advice = new ArrayList<>();

        String optimizedTitle = title;
        if (title.length() < 6) {
            optimizedTitle = category.getName() + "｜" + (title.isBlank() ? "请填写品牌型号和成色" : title);
            advice.add("标题建议包含品牌/型号、成色和核心卖点");
        } else if (!title.contains("新") && containsAny(description, "全新", "九成新", "八成新")) {
            String condition = containsAny(description, "全新", "未拆封") ? "全新"
                    : containsAny(description, "九成新", "9成新") ? "九成新" : "八成新";
            optimizedTitle = condition + "｜" + title;
        }
        if (optimizedTitle.length() > 100) optimizedTitle = optimizedTitle.substring(0, 100);

        String optimizedDescription = description;
        if (description.length() < 20) {
            optimizedDescription = description
                    + (description.isBlank() ? "" : "\n")
                    + "成色：请补充实际使用情况\n配件：请说明配件是否齐全\n交易方式：建议注明校内自提地点";
            advice.add("描述少于 20 字，建议补充成色、使用时长、配件和交易地点");
        }
        if (!containsAny(description, "划痕", "损坏", "维修", "瑕疵", "无拆修")) {
            advice.add("建议如实说明是否存在划痕、维修或功能异常");
        }
        if (!containsAny(description, "自提", "面交", "交易")) {
            advice.add("建议补充校内交易方式和可交易时间");
        }
        if (request.originalPrice() == null) advice.add("填写原价可获得更准确的价格评估");
        if (request.expectedPrice() != null
                && (request.expectedPrice().compareTo(price.minPrice()) < 0
                || request.expectedPrice().compareTo(price.maxPrice()) > 0)) {
            advice.add("期望售价不在建议区间内，可结合成色和同类价格调整");
        }
        boolean canPublish = !title.isBlank() && title.length() >= 4
                && description.length() >= 15
                && request.originalPrice() != null;
        if (canPublish) advice.add("基础信息较完整，可以发布并等待平台审核");
        else advice.add("信息尚不完整，建议补充后再发布");

        return new PublishAdviceVo(optimizedTitle, optimizedDescription, price.suggestedPrice(),
                "¥" + price.minPrice() + " - ¥" + price.maxPrice(), advice, canPublish);
    }

    public AgentChatVo chat(AgentChatRequest request, Long userId) {
        AgentModelProvider provider = modelProviders.orderedStream()
                .filter(AgentModelProvider::available).findFirst().orElse(null);
        if (provider != null) return provider.chat(request, userId);

        String question = request.question().trim();
        if (containsAny(question, "值得买吗", "能买吗", "是否值得")) {
            Goods goods = requireGoods(request.goodsId());
            RiskAnalyzeVo risk = riskAnalyzeService.analyze(goods.getId());
            return new AgentChatVo("该商品当前风险等级为 " + risk.riskLevel() + "（" + risk.score()
                    + " 分）。" + risk.advice(), risk.reasons(),
                    recommendationService.byCategory(userId, goods.getCategoryId(), 4));
        }
        if (containsAny(question, "价格合理", "价格怎么样", "贵不贵")) {
            Goods goods = requireGoods(request.goodsId());
            PriceAdviceVo price = priceAdvisorService.advise(
                    new PriceAdviceRequest(goods.getCategoryId(), goods.getOriginalPrice(), goods.getDescription()));
            String answer = goods.getPrice().compareTo(price.maxPrice()) > 0 ? "当前售价高于建议区间。"
                    : goods.getPrice().compareTo(price.minPrice()) < 0 ? "当前售价低于建议区间，请同时关注交易风险。"
                    : "当前售价位于建议区间内，价格相对合理。";
            return new AgentChatVo(answer + " 建议区间为 ¥" + price.minPrice() + " - ¥"
                    + price.maxPrice() + "。" + price.reason(),
                    List.of("结合实物成色确认最终价格", "优先选择校内当面验货"),
                    recommendationService.byCategory(userId, goods.getCategoryId(), 4));
        }
        if (containsAny(question, "推荐", "数码", "教材", "运动", "生活用品", "服饰")) {
            Long categoryId = findCategory(question);
            List<RecommendGoodsVo> related = categoryId == null
                    ? recommendationService.recommend(userId)
                    : recommendationService.byCategory(userId, categoryId, 8);
            return new AgentChatVo(related.isEmpty() ? "暂时没有符合条件的在售商品。"
                    : "我根据你的问题和平台在售数据找到了 " + related.size() + " 件商品。",
                    List.of("可点击商品查看详情和风险分析", "交易前建议当面验货"), related);
        }
        if (containsAny(question, "卖多少钱", "应该卖", "定价")) {
            if (request.goodsId() != null) {
                Goods goods = requireGoods(request.goodsId());
                PriceAdviceVo price = priceAdvisorService.advise(
                        new PriceAdviceRequest(goods.getCategoryId(), goods.getOriginalPrice(), goods.getDescription()));
                return new AgentChatVo("建议售价约 ¥" + price.suggestedPrice() + "，合理区间为 ¥"
                        + price.minPrice() + " - ¥" + price.maxPrice() + "。" + price.reason(),
                        List.of("描述中写明成色可提升评估准确度", "参考同类商品后保留少量议价空间"),
                        recommendationService.byCategory(userId, goods.getCategoryId(), 4));
            }
            return new AgentChatVo("请在问题中关联一个商品，或在发布页面填写分类、原价和成色后使用“智能生成发布建议”。",
                    List.of("提供商品原价", "说明全新/九成新/瑕疵等成色", "选择正确分类"), List.of());
        }
        if (containsAny(question, "提高信用", "信用分", "信誉")) {
            return new AgentChatVo("信用分由完成订单后的买家评价动态调整。稳定完成交易、如实描述商品并获得 4-5 星评价，可以逐步提升信用分。",
                    List.of("如实描述瑕疵，减少交易争议", "按约定时间和地点完成交易", "及时沟通并妥善包装商品",
                            "5 星评价 +2 分，4 星评价 +1 分"), List.of());
        }
        return new AgentChatVo("我是本地规则版智能交易助手。你可以问我商品是否值得买、价格是否合理、商品定价、分类推荐或如何提高信用分。",
                List.of("这个商品值得买吗", "这个价格合理吗", "给我推荐一些数码产品", "怎么提高信用分"),
                recommendationService.recommend(userId).stream().limit(4).toList());
    }

    private Goods requireGoods(Long goodsId) {
        if (goodsId == null) throw new BusinessException("请提供需要分析的商品");
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        return goods;
    }

    private Long findCategory(String question) {
        return categoryMapper.selectList(null).stream()
                .filter(category -> question.contains(category.getName())
                        || ("数码电子".equals(category.getName()) && question.contains("数码"))
                        || ("教材资料".equals(category.getName()) && question.contains("教材"))
                        || ("运动器材".equals(category.getName()) && question.contains("运动")))
                .map(Category::getId).findFirst().orElse(null);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) if (text.contains(keyword)) return true;
        return false;
    }
}
