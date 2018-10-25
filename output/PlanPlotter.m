
clear all;
clc;
%close all;
fid = fopen('MatlabDownload.txt', 'r');

tline = fgetl(fid);
%tline = replace(tline, ' ', '');
Firstline = strsplit(tline, ' ');
Ndown = str2double(cell2mat(Firstline(1))); % all chosen downloads
Ndowntot = str2double(cell2mat(Firstline(2))); % all possible downloads
NUsers = str2double(cell2mat(Firstline(3))); % Number of users
UserQuota = zeros(1,NUsers);
tline = fgetl(fid);
Secondline = strsplit(tline, ' ');
for i = 1:NUsers
    UserQuota(i) =  str2double(cell2mat(Secondline(i))); 
end

sizeA = [2 Ndown];
Downloadvar = fscanf(fid,'%f %f\n',sizeA);
fclose(fid);

disp(Downloadvar(1,:)); % display quota
disp(Downloadvar(2,:)); % display priority

UserName = cell(1,NUsers)
%UserName = {};
UserId = zeros(1,NUsers);
for i = 1:NUsers
    UserId(i) = i; 
    UserName(1,i) = {strcat('User ',num2str(i))};
end

Users = categorical(Downloadvar(1,:),UserId,UserName);

%%

h = histogram(Users,'BarWidth',0.5);


%%
clear all;
clc;
fid = fopen('MatlabAcquisition.txt', 'r');

tline = fgetl(fid);
Firstline = strsplit(tline, ' ');
Nacq = str2double(cell2mat(Firstline(1))); % all chosen acquisitions
Nacqtot = str2double(cell2mat(Firstline(2))); % all possible acquisitions
Ttrans = str2double(cell2mat(Firstline(3))); % Total transition time
TAcqtot = str2double(cell2mat(Firstline(4))); % Total Acquisition time
NUsers = str2double(cell2mat(Firstline(5))); % Number of users
UserQuota = zeros(1,NUsers);
tline = fgetl(fid);
Secondline = strsplit(tline, ' ');
for i = 1:NUsers
    UserQuota(i) =  str2double(cell2mat(Secondline(i))); 
end
sizeB = [4 Nacq];
Acquisitionvar = fscanf(fid,'%f %f %f %f\n',sizeB);
fclose(fid);

disp(Acquisitionvar(1,:)); % display cloud probability
disp(Acquisitionvar(2,:)); % display zenith angle
disp(Acquisitionvar(3,:)); % display priority
disp(Acquisitionvar(4,:)); % display user

%%

x = randn(2000,1);
y = 1 + randn(5000,1);
h1 = histogram(x);
hold on
h2 = histogram(y);


% figure
% positions = positions .* 0.001;
% plot(positions(:,1), positions(:,2), 'r*-')
% axis equal tight 
% grid on
% grid minor
% set(gca, 'FontSize', 14);
% xlabel('x[m]','FontSize', 16);
% ylabel('y[m]','FontSize', 16);
% title('Trajetoria teste 3 (c/ magnetometro)','FontSize', 20);