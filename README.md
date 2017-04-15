# LotteryView

因为会关注QQ运动的步数，偶然间在QQ运动主页--> 个人中心 --> 积分抽奖 看到抽奖的界面比较美观，
于是自行实现一下，原界面是在WebView中显示， 这里尝试使用Android自定义View实现类似的界面和操作逻辑。

支持Padding属性

抽离出以下自定义属性，如果有需要还可以自行添加更多自定义属性

<declare-styleable name="LotteryView">
        <attr name="outer_small_circle_color_active" format="color"></attr>  <!-- 小圆圈变化的颜色 -->
        <attr name="outer_small_circle_color_default" format="color"></attr> <!-- 小圆圈默认颜色 -->
        <attr name="lottery_invalidate_times" format="integer"></attr>       <!-- 抽奖动画刷新次数 -->
        <attr name="self_width_size_factor" format="float"></attr>           <!-- view宽度占屏幕宽度的比例 建议设置此值不小于0.75f -->
        <attr name="inner_round_card_color_default" format="color"></attr>   <!-- 内部卡片默认背景颜色 -->
    </declare-styleable>

如下是效果截图 ，如果看不清楚gif图 在demoVideo文件夹下中有一段演示视频供参考。

![image](https://github.com/aquarius520/LotteryView/tree/master/images/Screenshot_20170417-221603.jpg)

![image](https://github.com/aquarius520/LotteryView/tree/master/images/Screenshot_20170417-221611.jpg)

![image](https://github.com/aquarius520/LotteryView/tree/master/images/Screenshot_20170417-221618.jpg)

![image](https://github.com/aquarius520/LotteryView/tree/master/images/Screenshot_20170417-221630.jpg)

![image](https://github.com/aquarius520/LotteryView/tree/master/images/Screenshot_20170417-221708.jpg)


![image](https://github.com/aquarius520/LotteryView/tree/master/images/demo.gif)
