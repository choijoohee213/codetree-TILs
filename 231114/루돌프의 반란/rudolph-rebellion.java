import java.io.*;
import java.util.*;

public class Main {
    static int n,m,p,c,d,cowX,cowY;
    static int[] score;
    static int[][] map, playerPos;
    static Set<Integer> exitedPlayer;
    static Map<Integer, Integer> knockDown;
    static int[] dx = {-1,-1,-1,0,1,1,1,0}, dy = {-1,0,1,1,1,0,-1,-1};

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine(), " ");
		StringBuilder sb = new StringBuilder();

        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        p = Integer.parseInt(st.nextToken());
        c = Integer.parseInt(st.nextToken());
        d = Integer.parseInt(st.nextToken());
        map = new int[n][n];
        score = new int[p+1];
        playerPos = new int[p+1][2];
        exitedPlayer = new HashSet<>();
        knockDown = new HashMap<>();

        st = new StringTokenizer(br.readLine());
        cowX = Integer.parseInt(st.nextToken()) - 1;
        cowY = Integer.parseInt(st.nextToken()) - 1;
        map[cowX][cowY] = -1;

        for (int i = 1; i <=p; i++) {
            st = new StringTokenizer(br.readLine());
            int num = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken()) - 1;
            int y = Integer.parseInt(st.nextToken()) - 1;

            map[x][y] = num;
            playerPos[num][0] = x;
            playerPos[num][1] = y;
        }

        while(m-- > 0 && exitedPlayer.size() < p) {
            int[] res = cowMove();
            int targetPlayer = findTargetPlayer(res[0]);
            int cowDir = res[1];

            crush(m, true, targetPlayer, cowDir);
            playerMove(m);

            for (int i = 1; i <= p; i++) {
                if(exitedPlayer.contains(i)) continue;
                score[i]++;
            }
        }

		br.close();
        for (int i = 1; i <= p; i++) {
            System.out.print(score[i] + " ");
        };
	}

    private static void playerMove(int time) {
        for (int i = 1; i <= p; i++) {
            if(exitedPlayer.contains(i)
                    || (knockDown.containsKey(i) && (knockDown.get(i) == time || knockDown.get(i)-1 == time))) {
                continue;
            }

            int dist = getDist(playerPos[i][0], cowX, playerPos[i][1], cowY);
            int dir = -1;

            for (int j = 1; j < 8; j+=2) {
                int nx = playerPos[i][0] + dx[j];
                int ny = playerPos[i][1] + dy[j];

                if(nx<0 || nx>=n || ny<0 || ny>=n || map[nx][ny]>0) continue;
                int nDist = getDist(nx, cowX, ny, cowY);

                if(dist > nDist) {
                    dist = nDist;
                    dir = j;
                }
            }

            if(dir != -1) {
                playerPos[i][0] += dx[dir];
                playerPos[i][1] += dy[dir];
                crush(time, false, i, dir);
            }

        }
    }

    //3. 충돌
    private static void crush(int time, boolean cowGoToPlayer, int player, int dir) {
        int[][] newMap = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newMap[i][j] = map[i][j];
            }
        }

        if(cowGoToPlayer) {
            newMap[cowX-dx[dir]][cowY-dy[dir]] = 0;
            newMap[cowX][cowY] = -1;
        } else {
            newMap[playerPos[player][0]-dx[dir]][playerPos[player][1]-dy[dir]] = 0;
        }


        int nx = playerPos[player][0], ny = playerPos[player][1];
        if (cowX == nx && cowY == ny) {
            if(cowGoToPlayer) {
                nx += dx[dir] * c;
                ny += dy[dir] * c;
                score[player] += c;
            } else {
                dir = (dir + 4) % 8;
                nx += dx[dir] * d;
                ny += dy[dir] * d;
                score[player] += d;
            }

            if(nx<0 || nx>= n || ny<0 || ny>=n) {
                exitedPlayer.add(player);
                map = newMap;
                return;
            }

            knockDown.put(player, time);

            newMap[nx][ny] = player;
            playerPos[player][0] = nx;
            playerPos[player][1] = ny;

            while(map[nx][ny]>0 && map[nx][ny] != player) {
                int nPlayer = map[nx][ny];
                nx += dx[dir];
                ny += dy[dir];

                if(nx<0 || nx>=n || ny<0 || ny>=n) {
                    exitedPlayer.add(nPlayer);
                    break;
                }

                playerPos[nPlayer][0] = nx;
                playerPos[nPlayer][1] = ny;
                newMap[nx][ny] = nPlayer;
                player = nPlayer;
            }
        } else if(!cowGoToPlayer) {
            newMap[playerPos[player][0]][playerPos[player][1]] = player;
        }

        map = newMap;
    }

    private static int findTargetPlayer(int player) {
        for (int i = 1; i <= p; i++) {
            if(exitedPlayer.contains(i)) continue;
            if(playerPos[i][0] == cowX && playerPos[i][1] == cowY) {
                return i;
            }
        }
        return player;
    }

    //1. 소 이동
    private static int[] cowMove() {
        int dist = Integer.MAX_VALUE, player = 0;

        for (int i = 1; i <= p; i++) {
            if(exitedPlayer.contains(i)) continue;

            int nDist = getDist(cowX, playerPos[i][0], cowY, playerPos[i][1]);
            if(dist > nDist) {
                dist = nDist;
                player = i;
            } else if(dist == nDist && ((playerPos[i][0] > playerPos[player][0])
                    || (playerPos[i][0] == playerPos[player][0] && playerPos[i][1] > playerPos[player][1]))) {
                dist = nDist;
                player = i;
            }
        }

        dist = Integer.MAX_VALUE;
        int dir = -1;
        for (int i = 0; i < 8; i++) {
            int nx = cowX + dx[i];
            int ny = cowY + dy[i];

            if(nx<0 || nx>=n || ny<0 || ny>=n) continue;
            int nDist = getDist(nx, playerPos[player][0], ny, playerPos[player][1]);

            if(dist > nDist) {
                dist = nDist;
                dir = i;
            }
        }

        cowX += dx[dir];
        cowY += dy[dir];
        return new int[]{player, dir};
    }

    private static int getDist(int x1, int x2, int y1, int y2) {
        return (x1-x2) * (x1-x2) + (y1-y2) * (y1-y2);
    }
}