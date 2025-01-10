package com.namdam1123.j2ee.postservicequerry.Entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostStatistic {
    public List<UUID> PostId;
    public int AverageLike;
    public LocalDateTime StartTime;
    public LocalDateTime EndTime;

}
